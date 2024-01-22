package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.UserHeart;
import com.chillin.hearting.db.repository.UserHeartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserHeartService {

    private final UserHeartRepository userHeartRepository;

    public List<UserHeart> findAllByUserIdOrderByHeartId(String userId) {
        return userHeartRepository.findAllByUserIdOrderByHeartId(userId);
    }

    public boolean isUserAcquiredHeart(String userId, long heartId) {
        Optional<UserHeart> result = userHeartRepository.findByHeartIdAndUserId(heartId, userId);
        return !result.isEmpty();
    }

    public UserHeart save(UserHeart userHeart) {
        return userHeartRepository.save(userHeart);
    }

    public Optional<UserHeart> findByHeartIdAndUserId(long heartId, String userId) {
        return userHeartRepository.findByHeartIdAndUserId(heartId, userId);
    }

}
