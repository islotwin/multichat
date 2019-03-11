package com.islotwin.multichat.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Accessors(chain = true)
@Document(collection = "messages")
@EqualsAndHashCode(of = "id")
public class MessageEntity {

    @Id
    private String id;

    @NotEmpty
    private String chatRoom;

    @NotEmpty
    private String sessionId;

    @NotEmpty
    private String text;

    @NotNull
    private Date timestamp;

    private String language;

    private Set<TranslatedMessage> translatedMessages = new HashSet<>();

}