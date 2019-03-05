package com.islotwin.multichat.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class MessageDetailsDto extends MessageDto {

    public MessageDetailsDto(final String text, final Date timestamp) {
        super(text, timestamp);
    }

    private String username;

    private String translatedText;

    private String originLanguage;

}