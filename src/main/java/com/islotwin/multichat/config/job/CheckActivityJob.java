package com.islotwin.multichat.config.job;

import com.islotwin.multichat.model.activity.ActivityRepository;
import com.islotwin.multichat.model.message.MessageRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Configuration
@EnableScheduling
public class CheckActivityJob {
    private final Long chatExpiration;
    private final ActivityRepository activityRepository;
    private final MessageRepository messageRepository;

    public CheckActivityJob(@Value("${chat.expiration}") final Long chatExpiration, final ActivityRepository activityRepository,
                            final MessageRepository messageRepository) {
        this.chatExpiration = chatExpiration;
        this.activityRepository = activityRepository;
        this.messageRepository = messageRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredChatRooms() {
        val threshold = LocalDateTime.now().minusDays(this.chatExpiration);
        val chatRoomsToDelete = activityRepository.findAll().stream()
                .filter(a -> a.getTimestamp().isBefore(threshold))
                .collect(Collectors.toList());
        val messagesToDelete = chatRoomsToDelete.stream()
                .flatMap(c -> messageRepository.findAllByChatRoom(c.getName()).stream())
                .collect(Collectors.toList());
        activityRepository.deleteAll(chatRoomsToDelete);
        messageRepository.deleteAll(messagesToDelete);
    }
}
