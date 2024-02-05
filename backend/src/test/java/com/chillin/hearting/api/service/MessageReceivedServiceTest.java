package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.MessageData;
import com.chillin.hearting.api.data.ReceivedMessageData;
import com.chillin.hearting.db.domain.Emoji;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.MessageRepository;
import com.chillin.hearting.exception.UnAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageReceivedServiceTest {
    @InjectMocks
    private MessageReceivedService messageReceivedService;
    @Mock
    private MessageRepository messageRepository;
    private final String senderId = "senderId";
    private final String receiverId = "receiverId";
    private final long messageId = 0L;
    private final User receiver = User.builder().id(receiverId).messageTotal(0L).build();
    private final Heart heart = Heart.builder().id(0L).name("testHeart").build();
    private final Message message = Message.builder().id(0L).receiver(receiver).build();

    @BeforeEach
    public void setupIsActive() {
        message.undeleteMessage();
    }


    // getReceivedMessages
    @Test
    void successGetReceivedMessages() {
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

        doReturn(messageList).when(messageRepository).findByReceiverIdAndIsActiveTrue(receiverId, Sort.by(Sort.Direction.DESC, "createdDate"));

        // when
        final ReceivedMessageData data = messageReceivedService.getReceivedMessages(receiverId, true);

        // then
        assertThat(data.getMessageList().size()).isEqualTo(2);

        // verify
        verify(messageRepository, times(1)).findByReceiverIdAndIsActiveTrue(receiverId, Sort.by(Sort.Direction.DESC, "createdDate"));
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void successGetReceivedMessagesWithEmoji() {
        // given
        List<Message> messageList = new ArrayList<>();
        Emoji emoji = Emoji.builder().id(1L).name("name").imageUrl("url").build();
        Message message1 = Message.builder().id(0L).heart(heart).receiver(receiver).emoji(emoji).expiredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(3)).build();

        message1.undeleteMessage();

        messageList.add(message1);

        doReturn(messageList).when(messageRepository).findByReceiverIdAndIsActiveTrue(receiverId, Sort.by(Sort.Direction.DESC, "createdDate"));

        // when
        final ReceivedMessageData data = messageReceivedService.getReceivedMessages(receiverId, true);

        // then
        assertThat(data.getMessageList().size()).isEqualTo(messageList.size());
        assertThat(data.getMessageList().get(0).getEmojiId()).isEqualTo(emoji.getId());
        assertThat(data.getMessageList().get(0).getEmojiName()).isEqualTo(emoji.getName());
        assertThat(data.getMessageList().get(0).getEmojiUrl()).isEqualTo(emoji.getImageUrl());

        // verify
        verify(messageRepository, times(1)).findByReceiverIdAndIsActiveTrue(receiverId, Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    @Test
    void successGetReceivedMessagesNoLogin() {
        // given
        List<Message> messageList = new ArrayList<>();
        Message message1 = Message.builder().id(0L).heart(heart).receiver(receiver).expiredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(3)).build();

        message1.undeleteMessage();

        messageList.add(message1);

        doReturn(messageList).when(messageRepository).findByReceiverIdAndIsActiveTrue(receiverId, Sort.by(Sort.Direction.DESC, "createdDate"));

        // when
        final ReceivedMessageData data = messageReceivedService.getReceivedMessages(receiverId, false);

        // then
        assertThat(data.getMessageList().get(0).isRead()).isEqualTo(message1.isRead());

        // verify
        verify(messageRepository, times(1)).findByReceiverIdAndIsActiveTrue(receiverId, Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    // getMessageDetail
    @Test
    void failGetMessageDetail_DifferentUser() {
        //given
        doReturn(Optional.of(message)).when(messageRepository).findById(messageId);

        // when
        UnAuthorizedException exception = assertThrows(UnAuthorizedException.class, () -> messageReceivedService.getMessageDetail(messageId, senderId));

        // then
        assertEquals("본인의 메시지만 상세열람할 수 있습니다.", exception.getMessage());
    }

    @Test
    void successGetMessageDetail() {
        // given
        long messageIdNoEmoji = 0L;
        long messageIdWithEmoji = 1L;
        Message messageNoEmoji = Message.builder().id(messageIdNoEmoji).heart(heart).receiver(receiver).build();
        Message savedMessageNoEmoji = Message.builder().isRead(true).id(messageIdNoEmoji).heart(heart).receiver(receiver).build();
        Emoji emoji = Emoji.builder().id(1L).name("name").imageUrl("url").build();
        Message messageWithEmoji = Message.builder().id(messageIdWithEmoji).heart(heart).receiver(receiver).emoji(emoji).build();
        Message savedMessageWithEmoji = Message.builder().isRead(true).id(messageIdWithEmoji).heart(heart).receiver(receiver).emoji(emoji).build();

        doReturn(Optional.of(messageNoEmoji)).when(messageRepository).findById(messageIdNoEmoji);
        doReturn(savedMessageNoEmoji).when(messageRepository).save(messageNoEmoji);
        doReturn(Optional.of(messageWithEmoji)).when(messageRepository).findById(messageIdWithEmoji);
        doReturn(savedMessageWithEmoji).when(messageRepository).save(messageWithEmoji);

        // when
        final MessageData dataNoEmoji = messageReceivedService.getMessageDetail(messageIdNoEmoji, receiverId);
        final MessageData dataWithEmoji = messageReceivedService.getMessageDetail(messageIdWithEmoji, receiverId);

        // then
        assertThat(dataNoEmoji.getMessageId()).isEqualTo(messageIdNoEmoji);
        assertThat(dataNoEmoji.isRead()).isTrue();
        assertThat(dataWithEmoji.getMessageId()).isEqualTo(messageIdWithEmoji);
        assertThat(dataWithEmoji.getEmojiId()).isEqualTo(emoji.getId());
        assertThat(dataWithEmoji.getEmojiName()).isEqualTo(emoji.getName());
        assertThat(dataWithEmoji.getEmojiUrl()).isEqualTo(emoji.getImageUrl());

        // verify
        verify(messageRepository, times(1)).findById(messageIdNoEmoji);
        verify(messageRepository, times(1)).findById(messageIdWithEmoji);
        verify(messageRepository, times(2)).save(any(Message.class));
    }

}
