package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
public class HeartChecker {

    private String userId;
    private HeartCheckStrategy heartCheckStrategy;
    private HeartCheckStrategyFactory strategyFactory;

    public HeartChecker(HeartCheckStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void init(String userId, long heartId) {
        this.userId = userId;
        this.heartCheckStrategy = strategyFactory.createHeartCheckStrategy(heartId);
    }

    public boolean isAcquirable() {
        return heartCheckStrategy.isAcquirable(userId);
    }

    public ArrayList<HeartConditionData> getAcqCondition() {
        return heartCheckStrategy.getAcqCondition(userId);
    }
}
