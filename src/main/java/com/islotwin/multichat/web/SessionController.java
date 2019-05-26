package com.islotwin.multichat.web;

import com.google.cloud.translate.Language;
import com.islotwin.multichat.domain.LanguageDto;
import com.islotwin.multichat.domain.MessageDetailsDto;
import com.islotwin.multichat.domain.TokenDto;
import com.islotwin.multichat.domain.UsernameDto;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.session.ChatRoom;
import com.islotwin.multichat.model.session.SessionEntity;
import com.islotwin.multichat.model.session.SessionRepository;
import com.islotwin.multichat.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
        return messageRepository.findAllByChatRoomOrderByTimestampDesc(name, pageable)
                .map(m -> {
                    val translation = translateService.translate(m, session);
                    val publisher = sessionRepository.findById(m.getSessionId());
                    val username = publisher
                            .map(SessionEntity::getUsername)
                            .orElse("");
                    val color = publisher.map(SessionEntity::getColor)
                            .orElse("");
                    return new MessageDetailsDto(m.getText(), m.getTimestamp())
                            .setId(m.getId())
                            .setUsername(username)
                            .setTranslatedText(translation)
                            .setOriginLanguage(m.getLanguage())
                            .setFrom(m.getSessionId())
                            .setColor(color)
                            .setOut(sessionId.equals(m.getSessionId()));
                });
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

    @PutMapping("/users/{session}/username")
    @Transactional
    public UsernameDto changeUsername(@PathVariable("session") final String sessionId, @RequestBody UsernameDto username) {
        val user = sessionRepository.findById(sessionId)
                .map(s -> s.setUsername(username.getUsername()))
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));
        sessionRepository.save(user);
        return username;
    }

    @PutMapping("/users/{session}/token")
    @Transactional
    public TokenDto changeToken(@PathVariable("session") final String sessionId, @RequestBody TokenDto token) {
        val user = sessionRepository.findById(sessionId)
                .map(s -> s.setToken(token.getToken()))
                .orElseThrow(() -> new RuntimeException("Session " + sessionId + " not found."));
        sessionRepository.save(user);
        return token;
    }

    private ChatRoom setLanguage(final SessionEntity session, final String chatRoom, final String language) {
        return session.getChatRooms().stream()
                .filter(c -> c.getName().equals(chatRoom))
                .findAny()
                .map(c -> c.setLanguage(language))
                .orElseThrow(() -> new RuntimeException("Chat room " + chatRoom + " in session " + session.getId() + " not found."));
    }
}
