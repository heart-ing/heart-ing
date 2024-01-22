package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.*;
import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.repository.*;
import com.chillin.hearting.exception.HeartNotFoundException;
import com.chillin.hearting.exception.RedisKeyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class HeartService {

    private final HeartRepository heartRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public static final String KEY_SEND_HEARTS_PREFIX = "userSentHeart:";
    public static final String KEY_RECEIVED_HEARTS_PREFIX = "userReceivedHeart:";

    private final MigrationService migrationService;

    /**
     * 유저가 보낸 메시지를 바탕으로 보낸 하트 개수를 업데이트합니다.
     *
     * @param userId
     * @param heartId
     */
    public void updateSentHeartCount(String userId, Long heartId) {
        log.info("Redis에 userSentHeart를 업데이트합니다. userId:{} heartId:{}", userId, heartId);
        try {
            updateUserSentHeartCnt(userId, heartId);
        } catch (RedisKeyNotFoundException e) {
            log.info(e.getMessage());
            migrationService.migrateUserSentHeart(userId);
        }
    }

    /**
     * 유저가 받은 메시지를 바탕으로 받은 하트 개수를 업데이트합니다.
     *
     * @param userId
     * @param heartId
     */
    public void updateReceivedHeartCount(String userId, Long heartId) {
        log.info("Redis에 userReceivedHeart를 업데이트합니다. userId:{} heartId:{}", userId, heartId);
        try {
            updateUserReceivedHeartCnt(userId, heartId);
        } catch (RedisKeyNotFoundException e) {
            log.info(e.getMessage());
            migrationService.migrateUserReceivedHeart(userId);
        }
    }

    @Transactional(readOnly = true)
    public List<Heart> findAllTypeHearts() {
        return heartRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Heart> findDefaultTypeHearts() {
        return heartRepository.findAllByType(HeartType.DEFAULT.name());
    }

    @Transactional(readOnly = true)
    public List<Heart> findSpecialTypeHearts() {
        return heartRepository.findAllByType(HeartType.SPECIAL.name());
    }

    @Transactional(readOnly = true)
    public Heart findById(Long id) {
        return heartRepository.findById(id).orElseThrow(HeartNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Heart> findAll() {
        return heartRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Heart> findAllByType(String type) {
        return heartRepository.findAllByType(type);
    }

    @Transactional(readOnly = true)
    public List<HeartConditionDTO> findDefaultHeartSentCount(String userId) {
        return heartRepository.findDefaultHeartSentCount(userId);
    }

    @Transactional(readOnly = true)
    public List<HeartCountDTO> findAllHeartSentCount(String userId) {
        return heartRepository.findAllHeartSentCount(userId);
    }

    @Transactional(readOnly = true)
    public List<HeartCountDTO> findAllHeartReceivedCount(String userId) {
        return heartRepository.findAllHeartReceivedCount(userId);
    }

    @Transactional(readOnly = true)
    public int getUserSentHeartCnt(String userId, Long heartId) {
        Integer cnt = (Integer) redisTemplate.opsForHash().get(KEY_SEND_HEARTS_PREFIX+userId, heartId.toString());
        return cnt == null ? 0 : cnt;
    }

    @Transactional(readOnly = true)
    public int getUserReceivedHeartCnt(String userId, Long heartId) {
        Integer cnt = (Integer) redisTemplate.opsForHash().get(KEY_RECEIVED_HEARTS_PREFIX+userId, heartId.toString());
        return cnt == null ? 0 : cnt;
    }

    @Transactional(readOnly = true)
    public void updateUserSentHeartCnt(String userId, Long heartId) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String key = KEY_SEND_HEARTS_PREFIX + userId;

        // update sent heart count
        if (!redisTemplate.hasKey(key)) throw new RedisKeyNotFoundException(key);
        hashOperations.put(key, heartId.toString(), ((Integer) hashOperations.get(key, heartId.toString())).longValue() + 1);
    }

    @Transactional
    public void updateUserReceivedHeartCnt(String userId, Long heartId) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String key = KEY_RECEIVED_HEARTS_PREFIX + userId;

        // update received heart count
        if (!redisTemplate.hasKey(key)) throw new RedisKeyNotFoundException(key);
        hashOperations.put(key, heartId.toString(), ((Integer) hashOperations.get(key, heartId.toString())).longValue() + 1);
    }

}
