package com.chillin.hearting.db.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    @DisplayName("perPersist")
    void prePersist() {
        // given
        User user = User.builder()
                .id("abc")
                .type("KAKAO")
                .email("email")
                .nickname("nickname")
                .build();

        Heart heart = Heart.builder()
                .name("호감 하트")
                .imageUrl("test.com")
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type("DEFAULT")
                .build();

        Message message = Message.builder()
                .title("title")
                .heart(heart)
                .receiver(user)
                .isActive(true)
                .build();

        Notification notification = Notification.builder().user(user).heart(heart).message(message).build();

        // when
        notification.prePersist();

        // then
        assertThat(notification.getExpiredDate()).isEqualTo(notification.getCreatedDate().plusHours(24));
    }
}