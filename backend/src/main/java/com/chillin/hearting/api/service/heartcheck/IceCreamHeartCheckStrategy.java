package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class IceCreamHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;
    private final MessageService messageService;

    private static final int HEART_ICE_CREAM_MAX_VALUE = 3;

    @Override
    public boolean isAcquirable(String userId) {
        // 아이스크림 하트  - 햇살 하트 3개 받기
        int receivedHeartCnt = messageService.getUserReceivedHeartCnt(userId, HeartInfo.SUNNY.getId());
        if (receivedHeartCnt < HEART_ICE_CREAM_MAX_VALUE) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        Heart heart = heartService.findById(HeartInfo.SUNNY.getId());
        int receivedHeartCnt = messageService.getUserReceivedHeartCnt(userId, HeartInfo.SUNNY.getId());
        result.add(
                HeartConditionData.of(heart,receivedHeartCnt,HEART_ICE_CREAM_MAX_VALUE)
        );

        return result;
    }
}
