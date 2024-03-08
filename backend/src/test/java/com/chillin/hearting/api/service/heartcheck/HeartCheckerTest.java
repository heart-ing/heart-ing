package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.data.HeartConditionData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeartCheckerTest {

    @InjectMocks
    private HeartChecker heartChecker;

    @Mock
    private HeartCheckStrategyFactory heartCheckStrategyFactory;

    @Test
    @DisplayName("초기화 테스트")
    void init() {
        // given
        doReturn(mock(HeartCheckStrategy.class)).when(heartCheckStrategyFactory).createHeartCheckStrategy(anyLong());

        // when
        heartChecker.init("userId",1L);

        // then
        verify(heartCheckStrategyFactory, times(1)).createHeartCheckStrategy(anyLong());
    }

    @Test
    @DisplayName("획득 가능 여부 조회")
    void isAcquirable() {
        // given
        HeartCheckStrategy mockStrategy = mock(HeartCheckStrategy.class);
        doReturn(mockStrategy).when(heartCheckStrategyFactory).createHeartCheckStrategy(anyLong());
        doReturn(true).when(mockStrategy).isAcquirable(anyString());
        heartChecker.init("userId",1L);

        // when
        boolean result = heartChecker.isAcquirable();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("획득 조건 조회")
    void getAcqCondition() {
        // given
        HeartCheckStrategy mockStrategy = mock(HeartCheckStrategy.class);
        doReturn(mockStrategy).when(heartCheckStrategyFactory).createHeartCheckStrategy(anyLong());
        doReturn(new ArrayList<HeartConditionData>()).when(mockStrategy).getAcqCondition(anyString());
        heartChecker.init("userId",1L);

        // when
        List<HeartConditionData> result = heartChecker.getAcqCondition();

        // then
        assertThat(result).isNotNull();
    }
}