package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.InboxData;
import com.chillin.hearting.db.domain.Emoji;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.InboxRepository;
import com.chillin.hearting.exception.MessageAlreadyExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageInboxServiceTest {

    @InjectMocks
    private MessageInboxService messageInboxService;

    @Mock
    private InboxRepository inboxRepository;

    private final String fakeSenderId = "1";
    private final String getFakeReceiverId = "2";
    private User user1 = createUser(fakeSenderId, "test1.com", "nick1");
    private User user2 = createUser(getFakeReceiverId, "test2.com", "nick2");
    private Heart heart = createDefaultHeart();
    private Emoji emoji = createEmoji();


    @Test
    @DisplayName("메시지 영구 보관 성공")
    void storeMessageSuccess() {
        // given
        Long fakeId = 1L;
        Message savedMessage = createMessage(fakeId);
        savedMessage.prePersist();

        // mocking
        when(inboxRepository.findById(any())).thenReturn(Optional.ofNullable(savedMessage));

        // when
        assertThat(savedMessage.isStored()).isFalse();
        messageInboxService.storeMessage(fakeId);

        // then
        Optional<Message> findMessage = inboxRepository.findById(savedMessage.getId());
        assertThat(findMessage.get().isStored()).isTrue();
    }

    @Test
    @DisplayName("메시지 영구 보관 실패")
    void storeMessageFail() {
        // given
        Message mockMessage = mock(Message.class);
        when(inboxRepository.findById(eq(1L))).thenReturn(Optional.ofNullable(mockMessage));
        when(mockMessage.getExpiredDate()).thenReturn(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(1));

        // when
        try {
            messageInboxService.storeMessage(1L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MessageAlreadyExpiredException.class);
        }

        // then
        verify(mockMessage, times(0)).toInbox();
    }

    @Test
    @DisplayName("영구보관 메시지 조회")
    void findInboxMessages() {
        // given
        Message m1 = createMessage(1L);
        Message m2 = createMessage(2L);
        m1.toInbox();
        m2.toInbox();
        List<Message> inboxList = new ArrayList<>();
        inboxList.add(m1);
        inboxList.add(m2);

        // mocking
        when(inboxRepository.findAllByReceiverIdAndIsStored(any(), any())).thenReturn(inboxList);

        // when
        List<InboxData> findList = messageInboxService.findInboxMessages(getFakeReceiverId);
        assertThat(findList).hasSameSizeAs(inboxList);
    }

    @Test
    @DisplayName("영구보관 메시지 상세 조회")
    void findInboxDetailMessage() {
        // given
        Long fakeMessageId = 1L;
        Message message = createMessage(fakeMessageId);
        message.toInbox();

        // mocking
        when(inboxRepository.findByIdAndReceiverIdAndIsStored(any(), any(), any())).thenReturn(Optional.of(message));

        // when
        Message findMessage = messageInboxService.findInboxDetailMessage(user2.getId(), fakeMessageId);

        // then
        assertThat(findMessage.getId()).isEqualTo(fakeMessageId);
    }

    @Test
    @DisplayName("메시지 영구 삭제")
    void deleteMessage() {
        // given
        Long fakeId = 1L;
        Message savedMessage = createMessage(fakeId);
        savedMessage.deleteInbox();

        // mocking
        when(inboxRepository.findById(any())).thenReturn(Optional.ofNullable(savedMessage));

        // when
        messageInboxService.deleteMessage(fakeId);

        // then
        Optional<Message> findMessage = inboxRepository.findById(fakeId);
        assertThat(findMessage.get().isActive()).isFalse();
    }


    User createUser(String id, String email, String nickname) {
        if ("".equals(email)) email = "test.com";
        if ("".equals(nickname)) nickname = "test-nick";
        User user = User.builder()
                .id(id)
                .type("ROLE_TEST")
                .email(email)
                .nickname(nickname)
                .build();
        return user;
    }

    Heart createDefaultHeart() {
        Heart heart = Heart.builder()
                .name("호감 하트")
                .imageUrl("test.com")
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type("DEFAULT")
                .build();

        return heart;
    }

    public Emoji createEmoji() {
        Emoji emoji = Emoji.builder()
                .name("emoji")
                .imageUrl("emojiUrl")
                .build();

        return emoji;
    }

    Message createMessage(Long id) {
        return Message.builder()
                .id(id)
                .heart(heart)
                .emoji(emoji)
                .sender(user1)
                .receiver(user2)
                .title("title")
                .content("content")
                .build();
    }
}
