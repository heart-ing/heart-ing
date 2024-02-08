package com.chillin.hearting.api.service.facade;

import com.chillin.hearting.api.data.EmojiData;
import com.chillin.hearting.api.data.ReportData;
import com.chillin.hearting.api.data.SendMessageData;
import com.chillin.hearting.api.service.*;
import com.chillin.hearting.db.domain.*;
import com.chillin.hearting.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageFacadeTest extends AbstractTestData {

    @InjectMocks
    private MessageFacade messageFacade;

    @Mock
    private UserService userService;

    @Mock
    private HeartService heartService;

    @Mock
    private MessageService messageService;

    @Mock
    private UserHeartService userHeartService;

    @Mock
    private HeartCheckService heartCheckService;

    @Mock
    private EmojiService emojiService;

    @Mock
    private ReportUserService reportUserService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MigrationService migrationService;

    @Mock
    private BlockedUserService blockedUserService;

    @Test
    @DisplayName("ADMIN 스케줄러 동작")
    void sendScheduledMessageToAdmin() {
        // given
        MessageFacade mockFacade = spy(messageFacade);
        doReturn(receiver).when(userService).findById(anyString());
        doNothing().when(mockFacade).sendScheduledMessages(eq(receiver));
        doReturn(List.of(message)).when(messageService).findByReceiverIdAndSenderIp(anyString(),anyString());
        doReturn(mock(Message.class)).when(messageService).save(eq(message));

        assertThat(message.isActive()).isTrue();

        // when
        mockFacade.sendScheduledMessageToAdmin();

        // then
        assertThat(message.isActive()).isFalse();
    }

    @Test
    @DisplayName("ADMIN 메시지 전송")
    void sendScheduledMessages() {
        // given
        long messageTotal= receiver.getMessageTotal();
        doReturn(mock(Heart.class)).when(heartService).findById(anyLong());
        doReturn(mock(Message.class)).when(messageService).save(any(Message.class));
        doReturn(mock(User.class)).when(userService).save(any(User.class));

        // when
        messageFacade.sendScheduledMessages(receiver);

        // then
        assertThat(receiver.getMessageTotal()).isEqualTo(messageTotal+5);
    }

   // sendMessage
    @Test
    @DisplayName("호감 메시지 전송 - Sender Null")
    void successSendMessageNullSender() {
        // given
        doReturn(receiver).when(userService).findById(eq(receiver.getId()));
        doReturn(like).when(heartService).findById(eq(like.getId()));
        Message message = createMessage(1L,like,null,receiver,null);
        doReturn(message).when(messageService).save(any(Message.class));
        doReturn(specialHeartList).when(heartService).findSpecialTypeHearts();
        /**
         * 하트 이미 획득 6L,7L,11L,12L,13L,14L / 미획득 8L, 9L, 10L
         */
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(7L));
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(11L));
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(12L));
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(13L));
        doReturn(true).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(14L));
        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(8L));
        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(9L));
        doReturn(false).when(userHeartService).isUserAcquiredHeart(eq(receiver.getId()),eq(10L));
        /**
         * 미획득 중 하트 획득 가능 9L, 10L
         */
        doReturn(false).when(heartCheckService).isUserAcquirableHeart(eq(receiver.getId()),eq(8L));
        doReturn(true).when(heartCheckService).isUserAcquirableHeart(eq(receiver.getId()),eq(9L));
        doReturn(true).when(heartCheckService).isUserAcquirableHeart(eq(receiver.getId()),eq(10L));
        /**
         * 획득 가능 중 24시간 내 알림 있음 9L
         */
        doReturn(true).when(notificationService).hasNotificationIn24Hour(contains("9"));
        doReturn(false).when(notificationService).hasNotificationIn24Hour(contains("10"));

        // when
        final SendMessageData sendMessageData = messageFacade.sendMessage(message.getHeart().getId(), null, message.getReceiver().getId(), message.getTitle(), message.getContent(), message.getSenderIp());

        // then
        verify(userHeartService,times(specialHeartList.size())).isUserAcquiredHeart(eq(receiver.getId()),anyLong());
        verify(heartCheckService,times(3)).isUserAcquirableHeart(eq(receiver.getId()),anyLong());
        verify(notificationService, times(2)).hasNotificationIn24Hour(anyString());
        verify(notificationService, times(1)).save(eq(receiver.getId()),eq(10L));
        verify(notificationService, times(1)).setNotificationFor24Hour(contains("10"));
        verify(userService, times(1)).findById(eq(receiver.getId()));
        verify(userService, times(1)).save(eq(receiver));
        verify(heartService, times(1)).findById(eq(like.getId()));
        verify(messageService, times(1)).save(any(Message.class));
        verify(notificationService, times(1)).save(any(Notification.class));
        verify(migrationService, times(1)).updateReceivedHeartCount(eq(receiver.getId()),eq(like.getId()));

        assertThat(sendMessageData.getMessageId()).isEqualTo(message.getId());
        assertThat(sendMessageData.getHeartId()).isEqualTo(message.getHeart().getId());
        assertThat(sendMessageData.getHeartName()).isEqualTo(message.getHeart().getName());
        assertThat(sendMessageData.getHeartUrl()).isEqualTo(message.getHeart().getImageUrl());
        assertThat(sendMessageData.isRead()).isFalse();
    }

    @Test
    @DisplayName("호감 메시지 전송 - Sender not null")
    void successSendMessageSender() {
        // given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(receiver).when(userService).findById(eq(receiver.getId()));
        doReturn(sender).when(userService).findById(eq(sender.getId()));
        doReturn(like).when(heartService).findById(eq(like.getId()));
        doReturn(message).when(messageService).save(any(Message.class));

        // when
        final SendMessageData sendMessageData = messageFacade.sendMessage(message.getHeart().getId(), message.getSender().getId(), message.getReceiver().getId(), message.getTitle(), message.getContent(), message.getSenderIp());

        // then
        verify(migrationService,times(1)).updateSentHeartCount(eq(message.getSender().getId()), eq(message.getHeart().getId()));
    }

    // deleteMessage
    @Test
    @DisplayName("메시지 삭제 성공")
    void deleteMessage() {
        //given
        User user = createUser(receiver.getId());
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(message.getId());
        doReturn(message).when(messageService).save(any(Message.class));

        // when
        boolean result = messageFacade.deleteMessage(message.getId(), user.getId());

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 유저 != 수신자")
    void deleteMessageFailUserNotReceiver() {
        // given
        User user = createUser(receiver.getId()+"---");
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(message.getId());

        // when
        assertThrows(UnAuthorizedException.class, () -> messageFacade.deleteMessage(message.getId(), user.getId()));
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 삭제된 메시지")
    void deleteMessageFailMessageNotActive() {
        // given
        User user = createUser(receiver.getId());
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(message.getId());
        message.deleteMessage();

        // when
        assertThrows(MessageAlreadyDeletedException.class, () -> messageFacade.deleteMessage(message.getId(), user.getId()));
    }

    @Test
    @DisplayName("메시지 신고 성공")
    void reportMessage() {
        //given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(eq(message.getId()));
        doReturn(message).when(messageService).save(eq(message));
        doReturn(sender).when(userService).findById(eq(message.getSender().getId()));
        sender.prePersist();
        doReturn(receiver).when(userService).findById(eq(message.getReceiver().getId()));
        doReturn(sender).when(userService).save(eq(sender));
        doReturn(Report.builder().id(1L).build()).when(reportUserService).save(any(Report.class));

        // when
        ReportData data = messageFacade.reportMessage(message.getId(), receiver.getId(), message.getContent());

        // then
        assertTrue(message.isReported());
        assertThat(sender.getReportedCount()).isEqualTo(1);
        assertThat(sender.getStatus()).isEqualTo('A');
        assertTrue(data.isLoggedInUser());

        // verify
        verify(messageService, times(1)).findById(message.getId());
        verify(userService, times(1)).findById(sender.getId());
        verify(userService, times(1)).findById(receiver.getId());
        verify(messageService, times(1)).save(any(Message.class));
        verify(userService, times(1)).save(any(User.class));
        verify(reportUserService, times(1)).save(any(Report.class));
        verify(blockedUserService, times(0)).save(any(BlockedUser.class));

        // when
        Message message2 = createMessage(2L,like,sender,receiver,null);
        doReturn(message2).when(messageService).findById(eq(message2.getId()));
        doReturn(message2).when(messageService).save(eq(message2));
        messageFacade.reportMessage(message2.getId(), receiver.getId(), message2.getContent());

        Message message3 = createMessage(3L,like,sender,receiver,null);
        doReturn(message3).when(messageService).findById(eq(message3.getId()));
        doReturn(message3).when(messageService).save(eq(message3));
        messageFacade.reportMessage(message3.getId(), receiver.getId(), message3.getContent());

        // then
        assertThat(sender.getReportedCount()).isEqualTo(3);
        assertThat(sender.getStatus()).isEqualTo('P');

        // when
        Message message4 = createMessage(4L,like,sender,receiver,null);
        doReturn(message4).when(messageService).findById(eq(message4.getId()));
        doReturn(message4).when(messageService).save(eq(message4));
        messageFacade.reportMessage(message4.getId(), receiver.getId(), message4.getContent());

        Message message5 = createMessage(5L,like,sender,receiver,null);
        doReturn(message5).when(messageService).findById(eq(message5.getId()));
        doReturn(message5).when(messageService).save(eq(message5));
        messageFacade.reportMessage(message5.getId(), receiver.getId(), message5.getContent());

        // then
        assertThat(sender.getReportedCount()).isEqualTo(5);
        assertThat(sender.getStatus()).isEqualTo('O');
    }

    @Test
    @DisplayName("메시지 신고 실패 - 송신자 익명")
    void reportMessageFailSenderNotLogin() {
        // given
        Message message = createMessage(1L,like,null,receiver,null);
        doReturn(message).when(messageService).findById(eq(message.getId()));
        doReturn(receiver).when(userService).findById(eq(message.getReceiver().getId()));

        // when
        ReportData reportData = messageFacade.reportMessage(message.getId(),receiver.getId(), message.getContent());

        // then
        assertThat(reportData.isLoggedInUser()).isFalse();
    }

    @Test
    @DisplayName("메시지 신고 실패 - 유저 != 수신자")
    void ReportMessageFailUserNotReceiver() {
        //given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(message.getId());
        doReturn(sender).when(userService).findById(message.getSender().getId());
        doReturn(receiver).when(userService).findById(message.getReceiver().getId());
        String differentId = "differentId";

        // when
        UnAuthorizedException exception = assertThrows(UnAuthorizedException.class, () -> messageFacade.reportMessage(message.getId(), differentId, message.getContent()));

        // then
        assertEquals(exception.getMessage(), "본인이 받은 메시지만 신고할 수 있습니다.");
    }

    @Test
    @DisplayName("메시지 신고 실패 - 이미 신고된 메시지")
    void failReportMessageFailAlreadyReported() {
        //given
        Message message = createMessage(1L,like,sender,receiver,null);
        message.reportMessage();
        doReturn(message).when(messageService).findById(message.getId());
        doReturn(sender).when(userService).findById(message.getSender().getId());
        doReturn(receiver).when(userService).findById(message.getReceiver().getId());

        // when
        assertThrows(MessageAlreadyReportedException.class, () -> messageFacade.reportMessage(message.getId(), message.getReceiver().getId(), message.getContent()));

    }

    @Test
    @DisplayName("메시지 신고 실패 - 송신자 조회 불가")
    void failReportMessageFailSenderNotFound() {
        //given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(message.getId());
        doReturn(receiver).when(userService).findById(message.getReceiver().getId());
        doThrow(UserNotFoundException.class).when(userService).findById(message.getSender().getId());

        // when
        assertThrows(UserNotFoundException.class, () -> messageFacade.reportMessage(message.getId(), message.getReceiver().getId(), message.getContent()));

    }

    @Test
    @DisplayName("메시지 좋아요 이모지 추가 - 로그인 유저")
    void addEmojiForLogin() {
        //given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(eq(message.getId()));
        doReturn(likeEmoji).when(emojiService).findById(eq(likeEmoji.getId()));
        doReturn(message).when(messageService).save(eq(message));

        // when
        EmojiData emojiData = messageFacade.addEmoji(message.getId(), receiver.getId(), likeEmoji.getId());

        // then
        assertThat(emojiData.getEmojiUrl()).isEqualTo(likeEmoji.getImageUrl());
        assertThat(emojiData.getSenderId()).isEqualTo(message.getSender().getId());

        // verify
        verify(messageService, times(1)).findById(eq(message.getId()));
        verify(emojiService, times(1)).findById(eq(likeEmoji.getId()));
        verify(messageService, times(1)).save(eq(message));
        verify(notificationService, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("메시지 좋아요 이모지 추가 - Sender Null")
    void addEmojiForNullSender() {
        // given
        Message message = createMessage(1L,like,null,receiver,null);
        doReturn(message).when(messageService).findById(eq(message.getId()));
        doReturn(likeEmoji).when(emojiService).findById(eq(likeEmoji.getId()));
        doReturn(message).when(messageService).save(eq(message));

        // when
        EmojiData emojiData = messageFacade.addEmoji(message.getId(), receiver.getId(), likeEmoji.getId());

        // then
        assertThat(emojiData.getEmojiUrl()).isEqualTo(likeEmoji.getImageUrl());
        assertThat(emojiData.getSenderId()).isNull();

        // verify
        verify(messageService, times(1)).findById(eq(message.getId()));
        verify(emojiService, times(1)).findById(eq(likeEmoji.getId()));
        verify(messageService, times(1)).save(eq(message));
        verify(notificationService, times(0)).save(any(Notification.class));
    }

    @Test
    @DisplayName("메시지 이모지 삭제 - 로그인 유저")
    void removeEmojiForLogin() {
        // given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(eq(message.getId()));
        doReturn(message).when(messageService).save(eq(message));

        // when
        EmojiData emojiData = messageFacade.addEmoji(message.getId(), receiver.getId(), -1);

        // then
        assertThat(emojiData.getEmojiUrl()).isNull();
        assertThat(emojiData.getSenderId()).isEqualTo(message.getSender().getId());
    }

    @Test
    @DisplayName("이모지 추가 실패 - 유저 != 수신자")
    void addEmojiFailUserNotReceiver() {
        //given
        Message message = createMessage(1L,like,sender,receiver,null);
        doReturn(message).when(messageService).findById(message.getId());
        doReturn(likeEmoji).when(emojiService).findById(likeEmoji.getId());
        String differentId = "differentId";

        // when
        assertThrows(UnAuthorizedException.class, () -> messageFacade.addEmoji(message.getId(), differentId, likeEmoji.getId()));
    }

}
