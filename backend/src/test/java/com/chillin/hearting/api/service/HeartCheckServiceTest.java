package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.HeartConditionData;
import com.chillin.hearting.api.service.heartcheck.HeartChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HeartCheckServiceTest {

    @InjectMocks
    HeartCheckService heartCheckService;

    @Mock
    private HeartChecker heartChecker;

    @Test
    @DisplayName("하트 획득 가능 여부 조회")
    void isUserAcquirableHeart() {
        // given
        doNothing().when(heartChecker).init(anyString(),anyLong());
        doReturn(true).when(heartChecker).isAcquirable();

        // when
        boolean result = heartCheckService.isUserAcquirableHeart("userId",1L);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("하트 획득 조건 검사")
    void getSpecialHeartAcqCondition() {
        // given
        doNothing().when(heartChecker).init(anyString(),anyLong());
        doReturn(new ArrayList<HeartConditionData>()).when(heartChecker).getAcqCondition();

        // when
        List<HeartConditionData> result = heartCheckService.getSpecialHeartAcqCondition("userId",1L);

        // then
        assertThat(result).isNotNull();
    }
}