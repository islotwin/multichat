package com.islotwin.multichat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.islotwin.multichat.config.date.DateDeserializer;
import com.islotwin.multichat.config.date.DateSerializer;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Configuration
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        val mapper = new ObjectMapper();
        val module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new DateSerializer(LocalDateTime.class));
        module.addDeserializer(LocalDateTime.class, new DateDeserializer(LocalDateTime.class));
        mapper.registerModule(module);
        return mapper;
    }

    @Bean
    public Translate translate() {
        return TranslateOptions.getDefaultInstance().getService();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }


}
