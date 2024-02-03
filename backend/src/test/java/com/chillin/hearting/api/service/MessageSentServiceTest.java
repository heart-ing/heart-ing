package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.Data;
import com.chillin.hearting.api.data.SendMessageData;
import com.chillin.hearting.api.data.SentMessageListData;
import com.chillin.hearting.db.domain.Emoji;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.repository.SentMessageRepository;
import com.chillin.hearting.exception.MessageAlreadyExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSentServiceTest {

    @InjectMocks
    private MessageSentService messageSentService;

    @Mock
    private SentMessageRepository sentMessageRepository;

    @Test
    @DisplayName("보낸 메시지 조회")
    void getSentMessages() {
        // given
        String userId = "userId";
        Message message = createMockMessage();
        doReturn(LocalDateTime.now()).when(message).getExpiredDate();

        List<Message> messages = List.of(message);
        doReturn(messages).when(sentMessageRepository).findBySenderIdAndExpiredDateAfterOrderByExpiredDate(eq(userId),any(LocalDateTime.class));

        // when
        SentMessageListData result = messageSentService.getSentMessages(userId);

        // then
        assertThat(result.getSentMessageList()).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("보낸 메시지 상세 조회 성공")
    void getSentMessageDetailSuccess() {
        // given
        String userId = "userId";
        long messageId = 1L;
        Message message = createMockMessage();
        doReturn(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(1)).when(message).getExpiredDate();
        doReturn("content").when(message).getContent();
        doReturn(Optional.of(message)).when(sentMessageRepository).findByIdAndSenderId(eq(messageId), eq(userId));

        // when
        Data result = messageSentService.getSentMessageDetail(userId,messageId);

        // then
        assertThat(result).isInstanceOf(Data.class);
    }

    @Test
    @DisplayName("보낸 메시지 상세 조회 실패 - 유효기간 만료")
    void getSentMessageDetailFail() {
        // given
        String userId = "userId";
        long messageId = 1L;
        Message message = mock(Message.class);
        doReturn(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(1)).when(message).getExpiredDate();
        doReturn(Optional.of(message)).when(sentMessageRepository).findByIdAndSenderId(eq(messageId), eq(userId));

        // when
        try {
            messageSentService.getSentMessageDetail(userId,messageId);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MessageAlreadyExpiredException.class);
        }

        // then
        verify(message, times(0)).getContent();
    }

    private Message createMockMessage() {
        Message message = mock(Message.class);
        Heart heart = mock(Heart.class);
        Emoji emoji = mock(Emoji.class);
        long messageId = 1L;
        String title = "title";
        String shortDescription = "sd";
        long heartId = 1L;
        String name = "name";
        String imageUrl = "imageUrl";

        doReturn(emoji).when(message).getEmoji();
        doReturn(heart).when(message).getHeart();
        doReturn(messageId).when(message).getId();
        doReturn(title).when(message).getTitle();
        doReturn(LocalDateTime.now()).when(message).getCreatedDate();
        doReturn(shortDescription).when(heart).getShortDescription();
        doReturn(heartId).when(heart).getId();
        doReturn(name).when(heart).getName();
        doReturn(imageUrl).when(heart).getImageUrl();

        return message;
    }
}