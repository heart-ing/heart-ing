package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class SunnyHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;
    private final MessageService messageService;

    private static final int HEART_SUNNY_MAX_VALUE = 5;

    @Override
    public boolean isAcquirable(String userId) {
        // 햇살 하트 - 노랑 하트 5개 보내기
        int yellowHeartSentCnt = messageService.getUserSentHeartCnt(userId, HeartInfo.YELLOW.getId());
        if (yellowHeartSentCnt < HEART_SUNNY_MAX_VALUE) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        Heart heart = heartService.findById(HeartInfo.YELLOW.getId());
        int sentHeartCnt = messageService.getUserSentHeartCnt(userId, HeartInfo.YELLOW.getId());
        result.add(
                HeartConditionData.of(heart,sentHeartCnt,HEART_SUNNY_MAX_VALUE)
        );

        return result;
    }
}
