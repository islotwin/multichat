package com.islotwin.multichat.model.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    Page<MessageEntity> findAllByChatRoomOrderByTimestampDesc(final String chatRoom, final Pageable pageable);

    List<MessageEntity> findAllByChatRoom(final String chatRoom);
}
