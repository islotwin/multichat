package com.islotwin.multichat.config;

import com.islotwin.multichat.model.session.SessionEntity;
import com.islotwin.multichat.model.session.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.*;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionListener {

    private final SessionRepository repository;
    private final String prefix = "/chat";

    @EventListener
    @Transactional
    public void handleSubscription(SessionSubscribeEvent event) {
        val headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        val username = Optional.ofNullable(headers.getUser()).map(u -> u.getName()).orElse("");
        val sessionId = headers.getSessionId();
        val subscriptionId = headers.getSubscriptionId();
        val destination = getChatRoom(headers);

        log.info("New subscription: '{}' '{}' '{}' '{}'.", username, sessionId, subscriptionId, destination);
        val session = Optional.ofNullable(sessionId)
                .map(repository::findById)
                .orElseThrow(() -> new IllegalStateException("Session id can't be null."))
                .orElse(new SessionEntity()
                        .setId(sessionId)
                        .setUsername(username));
        session.addChatRoom(destination);
        repository.save(session);
    }

    @EventListener
    @Transactional
    public void handleUnsubscription(SessionUnsubscribeEvent event) {
        val headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        val sessionId = headers.getSessionId();
        val destination = getChatRoom(headers);

        log.info("Lost subscription: '{}' '{}'.", sessionId, destination);
        val session = Optional.ofNullable(sessionId)
                .flatMap(repository::findById)
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));

        session.deleteChatRoom(destination);
        repository.save(session);
    }

    @EventListener
    @Transactional
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        val headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        val sessionId = headers.getSessionId();

        log.info("Lost session: '{}'.", sessionId);
        val session = Optional.ofNullable(sessionId)
                .flatMap(repository::findById)
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));

        session.deleteChatRooms();
        repository.save(session);
    }

    private String getChatRoom(final SimpMessageHeaderAccessor headers) {
//        return headers.getDestination().replaceFirst(prefix + "/", "");

        return headers.getDestination().replaceAll("^.*" + prefix + "/(.*)", "$1");
    }

}
