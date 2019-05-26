package com.islotwin.multichat.web;

import com.islotwin.multichat.domain.MessageDto;
import com.islotwin.multichat.model.activity.ActivityEntity;
import com.islotwin.multichat.model.activity.ActivityRepository;
import com.islotwin.multichat.model.message.MessageEntity;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.session.SessionRepository;
import com.islotwin.multichat.service.MessageService;
import com.islotwin.multichat.service.TranslateService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class MessageController {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final ActivityRepository activityRepository;
    private final TranslateService translateService;
    private final MessageService messageService;

    public MessageController(final SessionRepository sessionRepository, final MessageRepository messageRepository,
                             final ActivityRepository activityRepository, final TranslateService translateService,
                             final MessageService messageService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.activityRepository = activityRepository;
        this.translateService = translateService;
        this.messageService = messageService;
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

        messageService.sendTranslatedMessages(chatRoom, entity, session);
    }

}