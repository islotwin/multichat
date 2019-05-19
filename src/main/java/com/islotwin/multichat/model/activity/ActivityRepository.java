package com.islotwin.multichat.model.activity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ActivityRepository extends MongoRepository<ActivityEntity, String> {

    Optional<ActivityEntity> findByName(final String chatRoom);

}
