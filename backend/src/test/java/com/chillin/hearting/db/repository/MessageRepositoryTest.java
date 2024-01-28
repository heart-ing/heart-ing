package com.chillin.hearting.db.repository;

import com.chillin.hearting.api.data.HeartCountDTO;
import com.chillin.hearting.api.service.AbstractTestData;
import com.chillin.hearting.db.domain.Emoji;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.Message;
import com.chillin.hearting.db.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest extends AbstractTestData {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HeartRepository heartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmojiRepository emojiRepository;

    private User savedSender;
    private User savedReceiver;
    private Heart savedHeart;
    private Emoji savedEmoji;

    @BeforeEach
    void setup() {
        savedSender = userRepository.save(createUser("sender"));
        savedReceiver = userRepository.save(createUser("receiver"));
        savedHeart = heartRepository.save(createHeart("name","DEFAULT"));
        savedEmoji = emojiRepository.save(createEmoji("name"));
    }

    @Test
    @DisplayName("메시지 저장")
    void save() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .build();
        // when
        Message savedMessage = messageRepository.save(message);

        // then
        assertThat(savedMessage.getId()).isEqualTo(message.getId());
    }

    @Test
    @DisplayName("메시지 삭제")
    void successDeleteMessage() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .build();

        // when
        Message messagePrev = messageRepository.save(message);

        // then
        assertThat(messagePrev.isActive()).isTrue();

        // when
        messagePrev.deleteMessage();
        messageRepository.flush();
        Message messageAfter = messageRepository.save(messagePrev);

        // then
        assertThat(messageAfter.getId()).isNotNull();
        assertThat(messageAfter.isActive()).isFalse();
    }

    @Test
    @DisplayName("메시지 신고")
    void successReportMessage() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .build();

        // when
        Message savedMessage = messageRepository.save(message);

        savedMessage.reportMessage();
        Message updatedMessage = messageRepository.save(savedMessage);

        // then
        assertThat(updatedMessage.isReported()).isTrue();
    }

    @Test
    @DisplayName("전체 메시지 조회")
    void findAll() {
        // given
        Message message1 = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .build();

        Message message2 = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .build();

        // when
        messageRepository.save(message1);
        messageRepository.save(message2);

        List<Message> messageList = messageRepository.findAll();

        // then
        assertThat(messageList.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("수신자 ID로 활성화 상태의 메시지 조회")
    void findByReceiverIdAndIsActiveTrue() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .build();

        messageRepository.save(message);

        // when
        List<Message> findMessageList = messageRepository.findByReceiverIdAndIsActiveTrue(savedReceiver.getId(), Sort.by(Sort.Direction.DESC, "createdDate"));

        // then
        assertThat(findMessageList).allMatch(msg -> msg.getReceiver().getId() == savedReceiver.getId() && msg.isActive() == true);
    }

    @Test
    @DisplayName("수신자 ID와 송신자 IP로 찾기")
    void findByReceiverIdAndSenderIp() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        messageRepository.save(message);

        // when
        List<Message> findMessageList = messageRepository.findByReceiverIdAndSenderIp(savedReceiver.getId(), message.getSenderIp());

        // then
        assertThat(findMessageList).allMatch(msg -> msg.getReceiver().getId() == savedReceiver.getId() && msg.getSenderIp() == msg.getSenderIp());

    }

    @Test
    @DisplayName("송신자 Ip를 제외한 null 포함 메시지 수")
    void countBySenderIpNotOrIsNull() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        Message adminMessage = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("ADMIN")
                .build();

        messageRepository.save(message);

        // when
        long count = messageRepository.countBySenderIpNotOrIsNull(adminMessage.getSenderIp());

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 사람에게 전송한 하트 별 메시지 수 중 최댓값")
    void findMaxMessageCountToSameUser() {
        // given
        Message message1 = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        Message message2 = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        Message message3 = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        messageRepository.save(message1);
        messageRepository.save(message2);
        messageRepository.save(message3);

        // when
        long result = messageRepository.findMaxMessageCountToSameUser(message1.getSender().getId(), savedHeart.getId());

        // then
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("유저의 하트 별 메시지 전송 개수")
    void findAllHeartSentCount() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        messageRepository.save(message);

        // when
        List<HeartCountDTO> findList = messageRepository.findAllHeartSentCount(message.getSender().getId());

        // then
        assertThat(findList).allMatch(dto -> (dto.getHeartId() == savedHeart.getId()) ? dto.getCurrentValue() == 1 : dto.getCurrentValue() == 0);
    }

    @Test
    @DisplayName("유저의 하트 별 메시지 수신 개수")
    void findAllHeartReceivedCount() {
        // given
        Message message = Message.builder()
                .title("title")
                .heart(savedHeart)
                .emoji(savedEmoji)
                .sender(savedSender)
                .receiver(savedReceiver)
                .senderIp("senderIp")
                .build();

        messageRepository.save(message);

        // when
        List<HeartCountDTO> findList = messageRepository.findAllHeartReceivedCount(message.getReceiver().getId());

        // then
        assertThat(findList).allMatch(dto -> (dto.getHeartId() == savedHeart.getId()) ? dto.getCurrentValue() == 1 : dto.getCurrentValue() == 0);
    }
}
