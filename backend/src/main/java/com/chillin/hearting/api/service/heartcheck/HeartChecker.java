package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;

import java.util.ArrayList;

public class HeartChecker {

    private HeartCheckStrategy heartCheckStrategy;
    private String userId;

    public HeartChecker(String userId) {
        this.userId = userId;
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
