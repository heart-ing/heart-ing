package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.*;
import com.chillin.hearting.db.repository.*;
import com.chillin.hearting.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;

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
}

