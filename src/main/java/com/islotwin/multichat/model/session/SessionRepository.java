package com.islotwin.multichat.model.session;

import com.islotwin.multichat.model.message.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;


public interface SessionRepository extends MongoRepository<SessionEntity, String> {

    Stream<SessionEntity> findAllByChatRoomsContaining(final ChatRoom chatRoom);
}
