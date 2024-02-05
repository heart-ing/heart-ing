package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;

import java.util.ArrayList;

public class HeartChecker {

    private HeartCheckStrategy heartCheckStrategy;
    private String userId;

    public HeartChecker(String userId, long heartId, HeartService heartService, MessageService messageService) {
        this.userId = userId;
        HeartCheckStrategyFactory strategyFactory = new HeartCheckStrategyFactory(heartService, messageService);
        setHeartCheckStrategy(strategyFactory.createHeartCheckStrategy(heartId));
    }

    public void setHeartCheckStrategy(HeartCheckStrategy strategy) {
        this.heartCheckStrategy = strategy;
    }

    public boolean isAcquirable() {
        return heartCheckStrategy.isAcquirable(userId);
    }

    public ArrayList<HeartConditionData> getAcqCondition() {
        return heartCheckStrategy.getAcqCondition(userId);
    }
}
