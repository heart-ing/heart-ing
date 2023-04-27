package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.ReceivedMessageData;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.HeartRepository;
import com.chillin.hearting.db.repository.MessageRepository;
import com.chillin.hearting.db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageReceivedServiceTest {
    @InjectMocks
    private MessageReceivedService messageReceivedService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HeartRepository heartRepository;

    @Mock
    private MessageRepository messageRepository;

    private final long heartId = 0L;
    private final String senderId = "senderId";
    private final String receiverId = "receiverId";
    private final String title = "title";
    private final String content = "content";
    private final String senderIp = "senderIp";
    private final long messageId = 0L;
    private final User receiver = User.builder().id(receiverId).messageTotal(0L).build();
    private final User sender = User.builder().id(senderId).build();
    private final Heart heart = Heart.builder().id(0L).name("testHeart").build();
    private final Message message = Message.builder().id(0L).receiver(receiver).build();

    @BeforeEach
    public void setupIsActive() {
        message.undeleteMessage();
    }


    // getReceivedMessages

    @Test
    void successGetReceivedMessage() {
        // given

        List<Message> messageList = new ArrayList<>();
        Message message1 = Message.builder().id(0L).heart(heart).receiver(receiver).expiredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(3)).build();
        Message message2 = Message.builder().id(0L).heart(heart).receiver(receiver).expiredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(3)).build();
        Message message3 = Message.builder().id(0L).heart(heart).receiver(receiver).expiredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul"))).build();
        Message message4 = Message.builder().id(0L).heart(heart).receiver(receiver).expiredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul"))).build();

        message1.undeleteMessage();
        message2.undeleteMessage();
        message3.undeleteMessage();
        message4.undeleteMessage();

        messageList.add(message1);
        messageList.add(message2);
        messageList.add(message3);
        messageList.add(message4);

        doReturn(messageList).when(messageRepository).findByReceiverIdAndIsActiveTrue(receiverId);

        // when
        final ReceivedMessageData data = messageReceivedService.getReceivedMessages(receiverId, true);


        // then
        assertThat(data.getMessageList().size()).isEqualTo(2);

        // verify
        verify(messageRepository, times(1)).findByReceiverIdAndIsActiveTrue(receiverId);
        verify(messageRepository, times(2)).save(any(Message.class));
    }

}
