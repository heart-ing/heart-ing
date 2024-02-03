package com.chillin.hearting.api.service;

import com.chillin.hearting.db.domain.UserHeart;
import com.chillin.hearting.db.repository.UserHeartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserHeartServiceTest extends AbstractTestData {

    @InjectMocks
    private UserHeartService userHeartService;

    @Mock
    private UserHeartRepository userHeartRepository;

    @Test
    @DisplayName("사용자 ID로 유저 하트 조회, 하트 ID로 정렬")
    void findAllByUserIdOrderByHeartId() {
        // given
        String userId = "userId";
        UserHeart userHeart1 = mock(UserHeart.class);
        UserHeart userHeart2 = mock(UserHeart.class);
        UserHeart userHeart3 = mock(UserHeart.class);
        List<UserHeart> userHeartList = List.of(userHeart1, userHeart2, userHeart3);
        doReturn(userHeartList).when(userHeartRepository).findAllByUserIdOrderByHeartId(eq(userId));

        // when
        List<UserHeart> result = userHeartService.findAllByUserIdOrderByHeartId(userId);

        // then
        assertThat(result).isEqualTo(userHeartList);
    }

    @Test
    @DisplayName("스페셜 하트 획득 여부 확인")
    void isUserAcquiredHeart() {
        // given
        long heartId = 1L;
        String userId = "userId";
        UserHeart userHeart = mock(UserHeart.class);
        doReturn(Optional.of(userHeart)).when(userHeartRepository).findByHeartIdAndUserId(eq(heartId),eq(userId));
        // when
        boolean result = userHeartService.isUserAcquiredHeart(userId,heartId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유저 하트 저장")
    void save() {
        // given
        UserHeart userHeart = mock(UserHeart.class);
        doReturn(userHeart).when(userHeartRepository).save(userHeart);
        // when

        UserHeart result = userHeartService.save(userHeart);

        // then
        assertThat(result).isEqualTo(userHeart);
    }

    @Test
    @DisplayName("획득한 스페셜 하트 조회")
    void findByHeartIdAndUserId() {
        // given
        String userId = "userId";
        long heartId = 1L;
        UserHeart userHeart = mock(UserHeart.class);
        doReturn(Optional.of(userHeart)).when(userHeartRepository).findByHeartIdAndUserId(eq(heartId),eq(userId));

        // when
        Optional<UserHeart> result = userHeartService.findByHeartIdAndUserId(heartId, userId);

        // then
        assertThat(result.get()).isEqualTo(userHeart);
    }
}