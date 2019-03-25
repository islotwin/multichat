package com.islotwin.multichat.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GCTranslateConfig {

    @Bean
    public Translate translate() {
        return TranslateOptions.getDefaultInstance().getService();
    }
}
