package com.chillin.hearting.db.repository;

import com.chillin.hearting.api.data.HeartConditionDTO;
import com.chillin.hearting.api.data.HeartCountDTO;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.exception.RedisKeyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class HeartRepository {

    private final HeartJPARepository heartJPARepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public static final String KEY_SEND_HEARTS_PREFIX = "userSentHeart:";
    public static final String KEY_RECEIVED_HEARTS_PREFIX = "userReceivedHeart:";

    public Optional<Heart> findById(Long id) {
        return heartJPARepository.findById(id);
    }

    public List<Heart> findAll() {
        return heartJPARepository.findAll();
    }

    public List<Heart> findAllByType(String type) {
        return heartJPARepository.findAllByType(type);
    }

    public List<HeartConditionDTO> findDefaultHeartSentCount(String userId) {
        return heartJPARepository.findDefaultHeartSentCount(userId);
    }

    public List<HeartCountDTO> findAllHeartSentCount(String userId) {
        return heartJPARepository.findAllHeartSentCount(userId);
    }

    public List<HeartCountDTO> findAllHeartReceivedCount(String userId) {
        return heartJPARepository.findAllHeartReceivedCount(userId);
    }

    public int getUserSentHeartCnt(String userId, Long heartId) {
        Integer cnt = (Integer) redisTemplate.opsForHash().get(KEY_SEND_HEARTS_PREFIX+userId, heartId.toString());
        return cnt == null ? 0 : cnt;
    }

    public int getUserReceivedHeartCnt(String userId, Long heartId) {
        Integer cnt = (Integer) redisTemplate.opsForHash().get(KEY_RECEIVED_HEARTS_PREFIX+userId, heartId.toString());
        return cnt == null ? 0 : cnt;
    }

    public void updateUserSentHeartCnt(String userId, Long heartId) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String key = KEY_SEND_HEARTS_PREFIX + userId;

        // update sent heart count
        if (!redisTemplate.hasKey(key)) throw new RedisKeyNotFoundException(key);
        hashOperations.put(key, heartId.toString(), ((Integer) hashOperations.get(key, heartId.toString())).longValue() + 1);
    }

    public void updateUserReceivedHeartCnt(String userId, Long heartId) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String key = KEY_RECEIVED_HEARTS_PREFIX + userId;

        // update received heart count
        if (!redisTemplate.hasKey(key)) throw new RedisKeyNotFoundException(key);
        hashOperations.put(key, heartId.toString(), ((Integer) hashOperations.get(key, heartId.toString())).longValue() + 1);
    }
}
