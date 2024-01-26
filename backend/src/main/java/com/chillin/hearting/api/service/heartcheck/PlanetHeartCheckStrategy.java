package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;

import java.util.ArrayList;

public class PlanetHeartCheckStrategy implements HeartCheckStrategy {
    @Override
    public boolean isAcquirable(String userId) {
        return false;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        return null;
    }
}
