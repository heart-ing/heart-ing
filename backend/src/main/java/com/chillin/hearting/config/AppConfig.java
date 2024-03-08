package com.chillin.hearting.config;

import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.heartcheck.HeartCheckStrategyFactory;
import com.chillin.hearting.api.service.heartcheck.HeartChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public HeartCheckStrategyFactory heartCheckStrategyFactory(HeartService heartService, MessageService messageService) {
        return new HeartCheckStrategyFactory(heartService, messageService);
    }

    @Bean
    public HeartChecker heartChecker(HeartCheckStrategyFactory heartCheckStrategyFactory) {
        return new HeartChecker(heartCheckStrategyFactory);
    }
}
