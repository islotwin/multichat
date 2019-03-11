package com.islotwin.multichat.model.message;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

//@Data
@Accessors(chain = true)
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "language")
public class ChatRoom {
    @Id
    private final String name;

    private String language;
}
