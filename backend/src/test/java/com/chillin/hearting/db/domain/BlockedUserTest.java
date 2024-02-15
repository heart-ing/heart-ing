package com.chillin.hearting.db.domain;

import com.chillin.hearting.exception.ServerLogicException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockedUserTest {

    @Test
    @DisplayName("유저 차단 종료일 설정 실패 - Active 설정")
    void updateEndDateFailStatusActive() {
        // given
        BlockedUser blockedUser = BlockedUser.builder().build();

        // when
        assertThrows(ServerLogicException.class, ()->blockedUser.updateEndDate('A'));
    }

    @Test
    @DisplayName("유저 차단 종료일 설정 실패 - 다른 상태 설정")
    void updateEndDateFailStrangeStatus() {
        // given
        BlockedUser blockedUser = BlockedUser.builder().build();

        // when
        assertThrows(ServerLogicException.class, ()->blockedUser.updateEndDate('C'));
    }
}