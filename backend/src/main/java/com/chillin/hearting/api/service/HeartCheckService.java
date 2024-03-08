package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.heartcheck.HeartChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeartCheckService {

    private final HeartChecker heartChecker;

    public boolean isUserAcquirableHeart(String userId, long heartId) {
        heartChecker.init(userId, heartId);

        return heartChecker.isAcquirable();
    }

    public List<HeartConditionData> getSpecialHeartAcqCondition(String userId, Long heartId) {
        heartChecker.init(userId, heartId);

        return heartChecker.getAcqCondition();
    }
}
