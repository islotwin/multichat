package com.islotwin.multichat.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MessageDetailsDto extends MessageDto {

    public MessageDetailsDto(final String text, final LocalDateTime timestamp) {
        super(text, timestamp);
    }

    private String id;

    private String username;

    private String from;

    private String translatedText;

    private String originLanguage;

    private String color;

    private Boolean out = false;

}