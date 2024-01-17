package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.enums.HeartInfo;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.repository.HeartRepository;
import com.chillin.hearting.db.repository.MessageRepository;
import com.chillin.hearting.exception.HeartNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class ReadingGlassesHeartCheckStrategy implements HeartCheckStrategy {

    private final HeartRepository heartRepository;
    private final MessageRepository messageRepository;

    private static final int HEART_READING_GLASSES_MAX_VALUE = 3;

    @Override
    public boolean isAcquirable(String userId) {
        // 돋보기 하트 - 특정인에게 핑크 하트 3개 보내기
        Integer result = messageRepository.findMaxMessageCountToSameUser(userId, HeartInfo.PINK.getId());
        int msgCnt = result == null ? 0 : result;
        if (msgCnt < HEART_READING_GLASSES_MAX_VALUE) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<HeartConditionData> getAcqCondition(String userId) {
        ArrayList<HeartConditionData> result = new ArrayList<>();

        Heart heart = heartRepository.findById(HeartInfo.PINK.getId()).orElseThrow(HeartNotFoundException::new);
        int sentHeartCnt = heartRepository.getUserSentHeartCnt(userId, HeartInfo.PINK.getId());
        result.add(
                HeartConditionData.of(heart,sentHeartCnt,HEART_READING_GLASSES_MAX_VALUE)
        );

        return result;
    }
}
