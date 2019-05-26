package com.islotwin.multichat.service;

import com.islotwin.multichat.domain.MessageDetailsDto;
import com.islotwin.multichat.domain.NotificationDto;
import com.islotwin.multichat.model.message.MessageEntity;
import com.islotwin.multichat.model.session.ChatRoom;
import com.islotwin.multichat.model.session.SessionEntity;
import com.islotwin.multichat.model.session.SessionRepository;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.stream.Stream;

@Service
public class MessageService {

    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final String prefix;
    private final WebClient webClient;
    private final TranslateService translateService;
    private final String notificationURL;

    public MessageService(final SessionRepository sessionRepository, final SimpMessagingTemplate messagingTemplate,
                          final TranslateService translateService, final WebClient webClient) {
        this.sessionRepository = sessionRepository;
        this.messagingTemplate = messagingTemplate;
        this.prefix = "/chat";
        this.translateService = translateService;
        this.webClient = webClient;
        this.notificationURL = "https://exp.host/--/api/v2/push/send";
    }

    public void sendTranslatedMessages(final String chatRoom, final MessageEntity message, final SessionEntity publisher) {
        getSubscribedSessions(chatRoom).parallel().forEach(s -> {
            val payload = createMessage(s, message, publisher);
            if (hasActiveSubscription(s, chatRoom)) {
                messagingTemplate.convertAndSendToUser(s.getId(), prefix + "/" + chatRoom, payload, createResponseHeaders(s.getId()));
            }
            if(s.getToken() != null && !s.getToken().isEmpty()) {
                sendNotification(s, chatRoom, payload);
            }
        });
    }

    private void sendNotification(final SessionEntity session, final String chatRoom, final MessageDetailsDto payload) {
        webClient.post()
                .uri(notificationURL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Encoding", "gzip", "deflate")
                .syncBody(new NotificationDto()
                        .setTo(session.getToken())
                        .setTitle(chatRoom)
                        .setBody(payload.getTranslatedText())
                        .setPayload(new NotificationDto.Payload(chatRoom)))
                .exchange();
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

    private Boolean hasActiveSubscription(final SessionEntity session, final String chatRoom) {
        return session.getChatRooms().stream()
                .filter(c -> c.getName().equals(chatRoom) && c.isActive())
                .findAny()
                .map(ChatRoom::isActive)
                .orElse(false);
    }

}
