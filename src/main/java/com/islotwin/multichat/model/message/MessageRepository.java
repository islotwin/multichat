package com.islotwin.multichat.model.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    Page<MessageEntity> findAllByChatRoom(final String chatRoom, final Pageable pageable);
}
