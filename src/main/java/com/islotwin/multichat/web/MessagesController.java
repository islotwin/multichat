package com.islotwin.multichat.web;

import com.islotwin.multichat.domain.MessageDetailsDto;
import com.islotwin.multichat.domain.MessageDto;
import com.islotwin.multichat.model.message.ChatRoom;
import com.islotwin.multichat.model.message.MessageEntity;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.session.SessionEntity;
import com.islotwin.multichat.model.session.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Stream;

@RestController
@Slf4j
public class MessagesController {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final String prefix;

    public MessagesController(final SessionRepository sessionRepository, final MessageRepository messageRepository,
                              final SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        prefix = "/topic/name";
    }

    @MessageMapping("/chat/{chatRoom}")
    @Transactional
    public void sendMessage(@DestinationVariable("chatRoom") final String chatRoom, @Payload final MessageDto message, final SimpMessageHeaderAccessor headerAccessor) {
        val sessionId = headerAccessor.getSessionId();
        val session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));

        val entity = new MessageEntity()
                .setChatRoom(chatRoom)
                .setSessionId(sessionId)
                .setLanguage("eng")
                .setTimestamp(message.getTimestamp())
                .setText(message.getText());
        messageRepository.save(entity);

        sendTranslatedMessages(chatRoom, entity, session);
    }

    private void sendTranslatedMessages(final String chatRoom, final MessageEntity message, final SessionEntity session) {
        getSubscribedSessions(chatRoom).parallel().forEach(s -> {
            val payload = createMessage(s, message, session);
            messagingTemplate.convertAndSend(prefix + "/" + chatRoom, payload, createResponseHeaders(s.getId()));
        });
    }

    private MessageDetailsDto createMessage(final SessionEntity subscriber, final MessageEntity message, final SessionEntity publisher) {
        val translation = translateText(message, subscriber);
        val username = publisher.getUsername() != null && !publisher.getUsername().isEmpty() ? publisher.getUsername() : "";
        return new MessageDetailsDto(message.getText(), message.getTimestamp())
                .setUsername(username)
                .setOriginLanguage(message.getLanguage())
                .setTranslatedText(translation);
    }

    private String translateText(final MessageEntity message, final SessionEntity user) {
        return message.getText();
    }

    private Map<String, Object> createResponseHeaders(final String sessionId) {
        val headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(sessionId);
        return headers.toMap();
    }

    private Stream<SessionEntity> getSubscribedSessions(final String name) {
        return sessionRepository.findAllByChatRoomsContaining(new ChatRoom(name));
    }

}