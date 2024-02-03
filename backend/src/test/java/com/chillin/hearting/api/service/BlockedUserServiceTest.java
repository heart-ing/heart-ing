package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.BlockedUser;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.BlockedUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockedUserServiceTest {

    @InjectMocks
    private BlockedUserService blockedUserService;

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @Test
    @DisplayName("악성 유저 저장")
    void save() {
        // given
        BlockedUser blockedUser = BlockedUser.builder()
                .user(mock(User.class))
                .startDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .endDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        doReturn(blockedUser).when(blockedUserRepository).save(blockedUser);

        // when
        BlockedUser savedBlockedUser = blockedUserService.save(blockedUser);

        // then
        assertThat(savedBlockedUser).isInstanceOf(BlockedUser.class);
    }
}