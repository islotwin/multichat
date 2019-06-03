package com.islotwin.multichat.service;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.islotwin.multichat.model.message.MessageEntity;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.message.TranslatedMessage;
import com.islotwin.multichat.model.session.ChatRoom;
import com.islotwin.multichat.model.session.SessionEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslateService {

    private final Translate translate;
    private final MessageRepository messageRepository;
    private final WebClient webClient;
    private final String deeplAuthKey = "62a22374-2040-ade1-2c33-c2d7b815f365";

    @Transactional
    public TranslatedMessage translate(final MessageEntity message, final SessionEntity session) {
        val translateTo = session.getChatRooms().stream()
                .filter(s -> s.getName().equals(message.getChatRoom()))
                .findAny()
                .map(ChatRoom::getLanguage)
                .orElse(message.getLanguage());
        return getTranslation(message, translateTo)
                .orElseGet(() -> {
                    val text = translate.translate(message.getText(), Translate.TranslateOption.targetLanguage(translateTo)).getTranslatedText();
                    val textDeepl = translateDeepl(message.getText(), translateTo);
                    val translatedMessage = new TranslatedMessage().setLanguage(translateTo).setText(text).setTextDeepl(textDeepl);
                    message.getTranslatedMessages().add(translatedMessage);
                    messageRepository.save(message);
                    return translatedMessage;
                });
    }

    public String detect(final String text) {
        return translate.detect(text).getLanguage();
    }

    public List<Language> getSupportedLanguages() {
        return translate.listSupportedLanguages();
    }

    private Optional<TranslatedMessage> getTranslation(final MessageEntity message, final String translateTo) {
        if(message.getLanguage().equals(translateTo)) {
            return Optional.of(new TranslatedMessage().setLanguage(message.getLanguage()).setText(message.getText()).setTextDeepl(message.getText()));
        }
        return message.getTranslatedMessages().stream()
                .filter(t -> t.getLanguage().equals(translateTo))
                .findAny();
    }

    private String translateDeepl(final String message, final String targetLanguage) {
        val deepl = buildBody(message, targetLanguage);
        return webClient.post()
                .uri("https://api.deepl.com/v2/translate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .syncBody(deepl)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMap(c -> c.bodyToMono(DeeplResponse.class))
                .map(l -> Optional.ofNullable(l.getTranslations()))
                .block()
                .flatMap(l -> Optional.ofNullable(l.get(0)))
                .map(DeeplTranslation::getText)
                .orElse("");
    }

    private MultiValueMap<String, String> buildBody(final String message, final String targetLanguage) {
        val body = new LinkedMultiValueMap<String, String>();
        body.add("text", message);
        body.add("target_lang", targetLanguage);
        body.add("auth_key", deeplAuthKey);
        return body;
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    private static class DeeplResponse {
        private List<DeeplTranslation> translations = new ArrayList();
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    private static class DeeplTranslation {
        private String detected_source_language;
        private String text;
    }

}
