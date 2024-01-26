package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.HeartCountDTO;
import com.chillin.hearting.db.domain.*;
import com.chillin.hearting.db.repository.*;
import com.chillin.hearting.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public static final String KEY_SEND_HEARTS_PREFIX = "userSentHeart:";
    public static final String KEY_RECEIVED_HEARTS_PREFIX = "userReceivedHeart:";

    public Integer findMaxMessageCountToSameUser(String userId, long heartId) {
        return messageRepository.findMaxMessageCountToSameUser(userId, heartId);
    }

    public Message save(Message m) {
        return messageRepository.save(m);
    }

    public List<Message> findByReceiverIdAndSenderIp(String adminId, String admin) {
        return messageRepository.findByReceiverIdAndSenderIp(adminId, admin);
    }

    public Message findById(long messageId) {
        return messageRepository.findById(messageId).orElseThrow(MessageNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<HeartCountDTO> findAllHeartSentCount(String userId) {
        return messageRepository.findAllHeartSentCount(userId);
    }

    @Transactional(readOnly = true)
    public List<HeartCountDTO> findAllHeartReceivedCount(String userId) {
        return messageRepository.findAllHeartReceivedCount(userId);
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
}

