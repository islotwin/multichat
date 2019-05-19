package com.islotwin.multichat.service;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.islotwin.multichat.model.session.ChatRoom;
import com.islotwin.multichat.model.message.MessageEntity;
import com.islotwin.multichat.model.message.MessageRepository;
import com.islotwin.multichat.model.message.TranslatedMessage;
import com.islotwin.multichat.model.session.SessionEntity;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final Translate translate;
    private final MessageRepository messageRepository;

    @Transactional
    public String translate(final MessageEntity message, final SessionEntity session) {
        val translateTo = session.getChatRooms().stream()
                .filter(s -> s.getName().equals(message.getChatRoom())).findAny()
                .map(ChatRoom::getLanguage)
                .orElse(message.getLanguage());
        return getTranslation(message, translateTo)
                .orElseGet(() -> {
                    val text = translate.translate(message.getText(), Translate.TranslateOption.targetLanguage(translateTo)).getTranslatedText();
                    val translatedMessage = new TranslatedMessage().setLanguage(translateTo).setText(text);
                    message.getTranslatedMessages().add(translatedMessage);
                    messageRepository.save(message);
                    return translatedMessage;
                })
                .getText();
    }

    public String detect(final String text) {
        return translate.detect(text).getLanguage();
    }

    public List<Language> getSupportedLanguages() {
        return translate.listSupportedLanguages();
    }

    private Optional<TranslatedMessage> getTranslation(final MessageEntity message, final String translateTo) {
        if(message.getLanguage().equals(translateTo)) {
            return Optional.of(new TranslatedMessage().setLanguage(message.getLanguage()).setText(message.getText()));
        }
        return message.getTranslatedMessages().stream()
                .filter(t -> t.getLanguage().equals(translateTo))
                .findAny();
    }

}
