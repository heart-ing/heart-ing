package com.chillin.hearting.db.repository;

import com.chillin.hearting.api.service.AbstractTestData;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // MySQL 사용한다
class UserRepositoryTest extends AbstractTestData {
    @Autowired
    private UserRepository userRepository;

    @Test
    void KakaoSignupSuccess() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        // given
        final User member = User.builder()
                .id("abc")
                .type("KAKAO")
                .email("wjdwn@naver.com")
                .nickname("jj")
                .build();

        // when
        userRepository.save(member);
        final User result = userRepository.findByEmailAndType("wjdwn@naver.com", "KAKAO").orElseThrow(NotFoundException::new);

        // then
        assertThat(result.getId()).isEqualTo("abc");
        assertThat(result.getType()).isEqualTo("KAKAO");
        assertThat(result.getEmail()).isEqualTo("wjdwn@naver.com");
        assertThat(result.getNickname()).isEqualTo("jj");
        assertThat(result.getReportedCount()).isZero();
        assertThat(result.getRole()).isEqualTo("ROLE_USER");

    }

    @Test
    @DisplayName("유저 메시지 수신 횟수 업데이트")
    void updateUserMessageTotal() {
        // given
        User savedUser = createUser("id");
        savedUser = userRepository.save(savedUser);
        long beforeMessageTotalCnt = savedUser.getMessageTotal();

        // when
        savedUser.updateMessageTotal();
        User findUser = userRepository.save(savedUser);

        // then
        assertThat(findUser.getMessageTotal()).isEqualTo(beforeMessageTotalCnt+1);
    }
}
