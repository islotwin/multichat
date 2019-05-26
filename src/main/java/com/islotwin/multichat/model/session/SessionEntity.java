package com.islotwin.multichat.model.session;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Accessors(chain = true)
@Document(collection = "sessions")
@EqualsAndHashCode(of = "id")
public class SessionEntity {

    @Id
    private String id;

    private String username;

    private String color;

    private String token;

    private Set<ChatRoom> chatRooms = new HashSet<>();

    public void activateChatRoom(final String chatRoom) {
        chatRooms.stream()
                .filter(c -> c.getName().equals(chatRoom))
                .findAny()
                .orElseGet(() -> {
                    val chat = new ChatRoom(chatRoom);
                    chatRooms.add(chat);
                    return chat;
                })
                .setActive(true);
    }

    public void deactivateChatRoom(final String chatRoom) {
        chatRooms.stream()
            .filter(c -> c.getName().equals(chatRoom))
            .findAny()
            .ifPresent(c -> c.setActive(false));
    }

    public void deactivateChatRooms() {
        chatRooms.forEach(c -> c.setActive(false));
    }

}