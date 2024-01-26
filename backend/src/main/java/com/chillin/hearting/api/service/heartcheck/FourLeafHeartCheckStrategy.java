package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class FourLeafHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;
    private final MessageService messageService;

    private static final int HEART_FOUR_LEAF_MAX_VALUE = 4;

    @Override
    public boolean isAcquirable(String userId) {
        // 네잎클로버 하트 - 세잎클로버 하트 4개 받기
        int shamrockHeartReceivedCnt = messageService.getUserReceivedHeartCnt(userId, HeartInfo.SHAMROCK.getId());
        if (shamrockHeartReceivedCnt < HEART_FOUR_LEAF_MAX_VALUE) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        Heart heart = heartService.findById(HeartInfo.SHAMROCK.getId());
        int receivedHeartCnt = messageService.getUserReceivedHeartCnt(userId, HeartInfo.SHAMROCK.getId());
        result.add(
                HeartConditionData.of(heart,receivedHeartCnt,HEART_FOUR_LEAF_MAX_VALUE)
        );

        return result;
    }
}
