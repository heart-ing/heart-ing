package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class ShamrockHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;
    private final MessageService messageService;

    private static final int HEART_SHAMROCK_MAX_VALUE = 3;

    @Override
    public boolean isAcquirable(String userId) {
        // 세잎클로버 하트 - 초록 하트 3개 보내기
        int greenHeartSentCnt = messageService.getUserSentHeartCnt(userId, HeartInfo.GREEN.getId());
        if (greenHeartSentCnt < HEART_SHAMROCK_MAX_VALUE) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        Heart heart = heartService.findById(HeartInfo.GREEN.getId());
        int sentHeartCnt = messageService.getUserSentHeartCnt(userId, HeartInfo.GREEN.getId());
        result.add(
                HeartConditionData.of(heart,sentHeartCnt,HEART_SHAMROCK_MAX_VALUE)
        );

        return result;
    }
}
