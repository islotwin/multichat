package com.islotwin.multichat.model.session;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

@Accessors(chain = true)
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"language", "isActive"})
public class ChatRoom {
    @Id
    private final String name;

    private String language;

    private boolean isActive = false;
}
