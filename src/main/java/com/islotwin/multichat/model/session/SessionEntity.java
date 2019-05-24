package com.islotwin.multichat.model.session;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
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

    private Set<ChatRoom> chatRooms = new HashSet<>();

    public void addChatRoom(final String chatRoom) {
        chatRooms.add(new ChatRoom(chatRoom));
    }

    public void addChatRoom(final String chatRoom, final String language) {
        chatRooms.add(new ChatRoom(chatRoom).setLanguage(language));
    }

    public void deleteChatRoom(final String chatRoom) {
        chatRooms.remove(new ChatRoom(chatRoom));
    }

    public void deleteChatRooms() {
        chatRooms.clear();
    }

}