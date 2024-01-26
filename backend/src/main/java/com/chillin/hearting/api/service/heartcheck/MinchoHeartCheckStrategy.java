package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class MinchoHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;
    private final MessageService messageService;

    private static final int HEART_MINCHO_MAX_VALUE = 5;

    @Override
    public boolean isAcquirable(String userId) {
        // 민초 하트 - 파란색 하트 5개 보내기
        int blueHeartSentCnt = messageService.getUserSentHeartCnt(userId, HeartInfo.BLUE.getId());
        if (blueHeartSentCnt < HEART_MINCHO_MAX_VALUE) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        Heart heart = heartService.findById(HeartInfo.BLUE.getId());
        int sentHeartCnt = messageService.getUserSentHeartCnt(userId, HeartInfo.BLUE.getId());
        result.add(
                HeartConditionData.of(heart,sentHeartCnt,HEART_MINCHO_MAX_VALUE)
        );

        return result;
    }
}
