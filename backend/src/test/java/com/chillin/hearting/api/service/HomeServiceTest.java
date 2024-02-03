package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.TotalMessageCountData;
import com.chillin.hearting.db.repository.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @InjectMocks
    private HomeService homeService;

    @Mock
    private MessageRepository messageRepository;

    @Test
    @DisplayName("전체 메시지 전송 수 조회")
    void totalMessageCount() {
        // given
        long count = 1L;
        doReturn(count).when(messageRepository).countBySenderIpNotOrIsNull(eq("ADMIN"));

        // when
        TotalMessageCountData result = homeService.totalMessageCount();

        // then
        assertThat(result.getTotalHeartCount()).isEqualTo(count);
    }
}