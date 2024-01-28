package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.HeartCountDTO;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.repository.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest extends AbstractTestData {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RedisTemplate redisTemplate;

    @Test
    @DisplayName("하트별 동일 사용자에게 보낸 최대 메시지 수")
    void findMaxMessageCountToSameUser() {
        // given
        doReturn(1).when(messageRepository).findMaxMessageCountToSameUser("sender",1L);

        // when
        int result = messageService.findMaxMessageCountToSameUser("sender",1L);

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("메시지 저장")
    void save() {
        // given
        doReturn(message).when(messageRepository).save(any(Message.class));

        // when
        Message savedMessage = messageService.save(Message.builder().build());

        // then
        assertThat(savedMessage).isEqualTo(message);
    }

    @Test
    @DisplayName("수신자 ID와 송신자 IP로 메시지 검색")
    void findByReceiverIdAndSenderIp() {
        // given
        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        doReturn(messageList).when(messageRepository).findByReceiverIdAndSenderIp("receiver","sender");

        // when
        List<Message> findMessageList = messageService.findByReceiverIdAndSenderIp("receiver","sender");

        // then
        assertThat(findMessageList).containsOnly(message);
    }

    @Test
    @DisplayName("아이디로 메시지 검색")
    void findById() {
        // given
        doReturn(Optional.of(message)).when(messageRepository).findById(message.getId());

        // when
        Message findMessage = messageService.findById(message.getId());

        // then
        assertThat(findMessage.getId()).isEqualTo(message.getId());
    }

    class TestHeartCountDto implements HeartCountDTO {
        long heartId;
        long currentValue;

        @Override
        public Long getHeartId() {
            return null;
        }

        @Override
        public Long getCurrentValue() {
            return null;
        }

        public TestHeartCountDto(Long heartId, Long currentValue) {
            this.heartId = heartId;
            this.currentValue = currentValue;
        }
    }
    @Test
    @DisplayName("사용자의 모든 하트 별 전송 메시지 수")
    void findAllHeartSentCount() {
        // given
        List<HeartCountDTO> dtoList = new ArrayList<>();
        dtoList.add(new TestHeartCountDto(1L,1L));
        dtoList.add(new TestHeartCountDto(2L,1L));
        dtoList.add(new TestHeartCountDto(3L,1L));
        doReturn(dtoList).when(messageRepository).findAllHeartSentCount(any(String.class));

        // when
        List<HeartCountDTO> result = messageService.findAllHeartSentCount("user");

        // then
        assertThat(result).allMatch(dto -> dto instanceof HeartCountDTO);
    }

    @Test
    @DisplayName("사용자의 모든 하트 별 전송 메시지 수")
    void findAllHeartReceivedCount() {
        // given
        List<HeartCountDTO> dtoList = new ArrayList<>();
        dtoList.add(new TestHeartCountDto(1L,1L));
        dtoList.add(new TestHeartCountDto(2L,1L));
        dtoList.add(new TestHeartCountDto(3L,1L));
        doReturn(dtoList).when(messageRepository).findAllHeartReceivedCount(any(String.class));

        // when
        List<HeartCountDTO> result = messageService.findAllHeartReceivedCount("user");

        // then
        assertThat(result).allMatch(dto -> dto instanceof HeartCountDTO);
    }

    @Test
    @DisplayName("유저가 특정 하트 메시지를 전송한 개수")
    void getUserSentHeartCnt() {
        // given
        notExistHeartId = 16L;
        existHeartId = 1L;
        int sentCnt = 1;
        HashOperations<String, String, Object> hashOperations = getMockRedisHashOperations();
        doReturn(hashOperations).when(redisTemplate).opsForHash();

        // when
        int resultForNotExistHeartId = messageService.getUserSentHeartCnt("userId",notExistHeartId);
        int resultForExistHeartId = messageService.getUserSentHeartCnt("userId",existHeartId);

        // then
        assertThat(resultForNotExistHeartId).isEqualTo(0);
        assertThat(resultForExistHeartId).isEqualTo(sentCnt);
    }

    @Test
    @DisplayName("유저가 특정 하트 메시지를 수신한 개수")
    void getUserReceivedHeartCnt() {
        // given
        notExistHeartId = 16L;
        existHeartId = 1L;
        int sentCnt = 1;
        HashOperations<String, String, Object> hashOperations = getMockRedisHashOperations();
        doReturn(hashOperations).when(redisTemplate).opsForHash();

        // when
        int resultForNotExistHeartId = messageService.getUserReceivedHeartCnt("userId",notExistHeartId);
        int resultForExistHeartId = messageService.getUserReceivedHeartCnt("userId",existHeartId);

        // then
        assertThat(resultForNotExistHeartId).isEqualTo(0);
        assertThat(resultForExistHeartId).isEqualTo(sentCnt);
    }

}