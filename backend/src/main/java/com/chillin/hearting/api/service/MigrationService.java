package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.HeartCountDTO;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.HeartRepository;
import com.chillin.hearting.db.repository.UserRepository;
import com.chillin.hearting.exception.RedisKeyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MigrationService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final HeartService heartService;
    private final MessageService messageService;
    private final UserService userService;

    private static final String KEY_SEND_HEARTS_PREFIX = "userSentHeart:";
    private static final String KEY_RECEIVED_HEARTS_PREFIX = "userReceivedHeart:";

    /**
     * MySQL에 저장된 모든 하트 정보를 Redis에 업데이트 합니다.
     */
    public void migrateHeartInfo() {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> map;
        for (Heart heart : heartService.findAll()) {
            map = new HashMap<>();
            map.put("id", heart.getId());
            map.put("name", heart.getName());
            map.put("imageUrl", heart.getImageUrl());
            map.put("shortDescription", heart.getShortDescription());
            map.put("longDescription", heart.getLongDescription());
            map.put("type", heart.getType());
            map.put("acqCondition", heart.getAcqCondition());
            hashOperations.putAll("heartInfo:" + heart.getId(), map);
        }

        log.info("모든 Heart Info 마이그레이션에 성공했습니다.");
    }

    /**
     * MySQL에 저장된 모든 하트 정보를 Redis에 업데이트 합니다.
     */
    public void migrateHeartList() {
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        listOperations.rightPush("heartList:default",1);
        listOperations.rightPush("heartList:default",2);
        listOperations.rightPush("heartList:default",3);
        listOperations.rightPush("heartList:default",4);
        listOperations.rightPush("heartList:default",5);

        log.info("모든 Heart List 마이그레이션에 성공했습니다.");
    }

    /**
     * MySQL의 모든 유저에 대해 User Sent Heart 수를 Redis에 업데이트 합니다.
     */
    public void migrateAllUserSentHeart() {
        List<User> userList = userService.findAll();
        for (User user : userList) {
            migrateUserSentHeart(user.getId());
        }

        log.info("{}명의 유저에 대해 User Sent Heart 마이그레이션에 성공했습니다.", userList.size());
    }

    /**
     * 한 명의 유저에 대해 User Sent Heart 수를 Redis에 업데이트 합니다.
     *
     * @param userId
     */
    public void migrateUserSentHeart(String userId) {
        HashOperations<String, String, Long> hashOperations = redisTemplate.opsForHash();
        String keyPrefix = KEY_SEND_HEARTS_PREFIX;
        List<HeartCountDTO> heartCountDTOList = messageService.findAllHeartSentCount(userId);
        for (HeartCountDTO dto : heartCountDTOList) {
            hashOperations.put(keyPrefix + userId, dto.getHeartId().toString(), dto.getCurrentValue());
        }
    }

    /**
     * MySQL의 모든 유저에 대해 User Received Heart 수를 Redis에 업데이트 합니다.
     */
    public void migrateAllUserReceivedHeart() {
        List<User> userList = userService.findAll();
        for (User user : userList) {
            migrateUserReceivedHeart(user.getId());
        }

        log.info("{}명의 유저에 대해 User Received Heart 마이그레이션에 성공했습니다.", userList.size());
    }

    /**
     * 한 명의 유저에 대해 User Received Heart 수를 Redis에 업데이트 합니다.
     *
     * @param userId
     */
    public void migrateUserReceivedHeart(String userId) {
        HashOperations<String, String, Long> hashOperations = redisTemplate.opsForHash();
        String keyPrefix = KEY_RECEIVED_HEARTS_PREFIX;
        List<HeartCountDTO> heartCountDTOList = messageService.findAllHeartReceivedCount(userId);
        for (HeartCountDTO dto : heartCountDTOList) {
            hashOperations.put(keyPrefix + userId, dto.getHeartId().toString(), dto.getCurrentValue());
        }
    }

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
            migrateUserSentHeart(userId);
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
            migrateUserReceivedHeart(userId);
        }
    }

    @Transactional
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
