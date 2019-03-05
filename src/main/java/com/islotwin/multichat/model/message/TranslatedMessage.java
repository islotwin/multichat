package com.islotwin.multichat.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = "language")
public class TranslatedMessage {

    private String language;

    private String text;

}