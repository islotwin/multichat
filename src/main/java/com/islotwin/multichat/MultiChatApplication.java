package com.islotwin.multichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class MultiChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiChatApplication.class, args);
	}

}
