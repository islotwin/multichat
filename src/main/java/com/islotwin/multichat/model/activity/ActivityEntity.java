package com.islotwin.multichat.model.activity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "activities")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = "name")
@RequiredArgsConstructor
// Keep latest message timestamp of the chat room
public class ActivityEntity {

    @Id
    private final String name;

    private LocalDateTime timestamp;
}
