package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;

import java.util.ArrayList;

public interface HeartCheckStrategy {
    boolean isAcquirable(String userId);
    ArrayList<HeartConditionData> getAcqCondition(String userId);
}
