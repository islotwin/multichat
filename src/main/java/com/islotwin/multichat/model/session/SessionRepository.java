package com.islotwin.multichat.model.session;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.stream.Stream;


public interface SessionRepository extends MongoRepository<SessionEntity, String> {

    @Query("{'chatRooms.name' : ?0}")
    Stream<SessionEntity> findAllByChatRooms(final String chatRoom);
}
