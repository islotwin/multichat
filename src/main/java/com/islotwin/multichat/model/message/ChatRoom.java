package com.islotwin.multichat.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
@EqualsAndHashCode(of = "name")
public class ChatRoom {
    private final String name;

    private String language;
}
