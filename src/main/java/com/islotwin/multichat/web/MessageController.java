package com.islotwin.multichat.web;

import com.islotwin.multichat.domain.MessageDetailsDto;
import com.islotwin.multichat.domain.MessageDto;
import com.islotwin.multichat.model.activity.ActivityEntity;
import com.islotwin.multichat.model.activity.ActivityRepository;
import com.islotwin.multichat.model.message.MessageEntity;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.session.SessionEntity;
import com.islotwin.multichat.model.session.SessionRepository;
import com.islotwin.multichat.service.TranslateService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
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
public class MessageController {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final ActivityRepository activityRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final String prefix;
    private final TranslateService translateService;

    public MessageController(final SessionRepository sessionRepository, final MessageRepository messageRepository,
                             final ActivityRepository activityRepository, final SimpMessagingTemplate messagingTemplate,
                             final TranslateService translateService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.activityRepository = activityRepository;
        this.messagingTemplate = messagingTemplate;
        this.prefix = "/chat";
        this.translateService = translateService;
    }

    @MessageMapping("/chat/{chatRoom}")
    @Transactional
    public void sendMessage(@DestinationVariable("chatRoom") final String chatRoom, @Payload final MessageDto message, @Header("simpSessionId") String sessionId) {
        val session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));

        val language = translateService.detect(message.getText());
        val entity = new MessageEntity()
                .setChatRoom(chatRoom)
                .setSessionId(sessionId)
                .setLanguage(language)
                .setTimestamp(message.getTimestamp())
                .setText(message.getText());
        messageRepository.save(entity);

        val activity = activityRepository
                .findByName(chatRoom)
                .orElse(new ActivityEntity(chatRoom))
                .setTimestamp(message.getTimestamp());
        activityRepository.save(activity);

        sendTranslatedMessages(chatRoom, entity, session);
    }

    private void sendTranslatedMessages(final String chatRoom, final MessageEntity message, final SessionEntity session) {
        getSubscribedSessions(chatRoom).parallel().forEach(s -> {
            val payload = createMessage(s, message, session);
            messagingTemplate.convertAndSendToUser(s.getId(), prefix + "/" + chatRoom, payload, createResponseHeaders(s.getId()));
        });
    }

    private MessageDetailsDto createMessage(final SessionEntity subscriber, final MessageEntity message, final SessionEntity publisher) {
        val translation = translateService.translate(message, subscriber);
        val username = publisher.getUsername() != null && !publisher.getUsername().isEmpty() ? publisher.getUsername() : "";
        val color = publisher.getColor();
        return new MessageDetailsDto(message.getText(), message.getTimestamp())
                .setId(message.getId())
                .setUsername(username)
                .setOriginLanguage(message.getLanguage())
                .setTranslatedText(translation)
                .setFrom(message.getSessionId())
                .setColor(color)
                .setOut(message.getSessionId().equals(subscriber.getId()));
    }

    private Map<String, Object> createResponseHeaders(final String sessionId) {
        val headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(sessionId);
        return headers.toMap();
    }

    private Stream<SessionEntity> getSubscribedSessions(final String name) {
        return sessionRepository.findAllByChatRooms(name);
    }

}