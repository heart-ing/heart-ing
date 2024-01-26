package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class RainbowHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;
    private final MessageService messageService;

    private static final int HEART_RAINBOW_MAX_VALUE = 1;

    @Override
    public boolean isAcquirable(String userId) {
        // 모든 기본 하트 1개 보내기
        for (Heart heart : heartService.findDefaultTypeHearts()) {
            int sentHeartCnt = messageService.getUserSentHeartCnt(userId, heart.getId());
            if (sentHeartCnt < HEART_RAINBOW_MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        for (Heart defaultHeart : heartService.findDefaultTypeHearts()) {
            int sentHeartCnt = messageService.getUserSentHeartCnt(userId, defaultHeart.getId());
            Heart heart = heartService.findById(defaultHeart.getId());
            result.add(
                    HeartConditionData.of(heart, sentHeartCnt, HEART_RAINBOW_MAX_VALUE)
            );
        }

        return result;
    }
}
