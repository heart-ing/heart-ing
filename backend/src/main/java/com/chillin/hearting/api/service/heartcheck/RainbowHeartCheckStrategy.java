package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.repository.HeartRepository;
import com.chillin.hearting.exception.HeartNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class RainbowHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartRepository heartRepository;

    private static final int HEART_RAINBOW_MAX_VALUE = 1;

    @Override
    public boolean isAcquirable(String userId) {
        // 모든 기본 하트 1개 보내기
        for (Heart heart : heartRepository.findAllByType(HeartType.DEFAULT.name())) {
            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, heart.getId());
            if (sentHeartCnt < HEART_RAINBOW_MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        for (Heart defaultHeart : heartRepository.findAllByType(HeartType.DEFAULT.name())) {
            int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, defaultHeart.getId());
            Heart heart = heartRepository.findById(defaultHeart.getId()).orElseThrow(HeartNotFoundException::new);
            result.add(
                    HeartConditionData.of(heart, sentHeartCnt, HEART_RAINBOW_MAX_VALUE)
            );
        }

        return result;
    }
}
