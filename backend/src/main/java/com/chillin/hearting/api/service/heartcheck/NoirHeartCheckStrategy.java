package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class NoirHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartService heartService;

    private static final int HEART_NOIR_MAX_VALUE = 2;

    @Override
    public boolean isAcquirable(String userId) {
        // 질투의 누아르 하트 - 모든 기본하트 2개 보내기
        for (Heart heart : heartService.findAllByType(HeartType.DEFAULT.name())) {
            int sentHeartCnt = heartService.getUserSentHeartCnt(userId, heart.getId());
            if (sentHeartCnt < HEART_NOIR_MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

//        for (Heart heart : heartRepository.findAllByType(HeartType.DEFAULT.name())) {
//            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, heart.getId());
//            result.add(
//                    HeartConditionData.of(heart,sentHeartCnt,HEART_NOIR_MAX_VALUE)
//            );
//        }

        return result;
    }
}
