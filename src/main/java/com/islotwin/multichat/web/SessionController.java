package com.islotwin.multichat.web;

import com.google.cloud.translate.Language;
import com.islotwin.multichat.domain.LanguageDto;
import com.islotwin.multichat.domain.MessageDetailsDto;
import com.islotwin.multichat.domain.UsernameDto;
import com.islotwin.multichat.model.message.ChatRoom;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.session.SessionEntity;
import com.islotwin.multichat.model.session.SessionRepository;
import com.islotwin.multichat.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final TranslateService translateService;

    @GetMapping("/languages")
    public List<Language> getLanguages() {
        return translateService.getSupportedLanguages();
    }

    @GetMapping("/users/{session}/chats/{name}")
    @Transactional(readOnly = true)
    public Page<MessageDetailsDto> getMessages(@PathVariable("name") final String name, @PathVariable("session") final String sessionId, final Pageable pageable) {
        val session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));
        val result = messageRepository.findAllByChatRoom(name, pageable).map(m -> {
            val translation = translateService.translate(m, session);
            val username = sessionRepository.findById(m.getSessionId())
                    .map(SessionEntity::getUsername)
                    .orElse("");
            return new MessageDetailsDto(m.getText(), m.getTimestamp())
                    .setUsername(username)
                    .setTranslatedText(translation)
                    .setOriginLanguage(m.getLanguage());
        });
        return new PageImpl<>(Collections.singletonList(new MessageDetailsDto("text", new Date())));
    }

    @PutMapping("/users/{session}/chats/{name}")
    @Transactional
    public ChatRoom changeLanguage(@PathVariable("name") final String name, @PathVariable("session") final String sessionId, @RequestBody LanguageDto language) {
        val session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));
        val chatRoom = setLanguage(session, name, language.getLanguage());
        sessionRepository.save(session);
        return chatRoom;
    }

    @PutMapping("/users/{session}")
    @Transactional
    public UsernameDto changeUsername(@PathVariable("session") final String sessionId, @RequestBody UsernameDto username) {
        val user = sessionRepository.findById(sessionId)
                .map(s -> s.setUsername(username.getUsername()))
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));
        sessionRepository.save(user);
        return username;
    }

    private ChatRoom setLanguage(final SessionEntity session, final String chatRoom, final String language) {
        return session.getChatRooms().stream()
                .filter(c -> c.getName().equals(chatRoom))
                .findAny()
                .map(c -> c.setLanguage(language))
                .orElseThrow(() -> new RuntimeException("Chat room " + chatRoom + " in session " + session.getId() + " not found."));
    }
}
