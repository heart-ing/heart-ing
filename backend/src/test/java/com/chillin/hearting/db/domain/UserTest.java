package com.chillin.hearting.db.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("유저 상태 활성화")
    void updateUserStatusToActive() {
        // given
        User user = User.builder().build();
        LocalDateTime localDateTime = LocalDateTime.of(9999, 12, 31, 0, 0);

        // when
        user.updateUserStatusToActive(localDateTime);

        // then
        assertThat(user.getStatus()).isEqualTo('A');
        assertThat(user.getUpdatedDate()).isEqualTo(localDateTime);
    }
}