package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.TotalMessageCountData;
import com.chillin.hearting.api.service.HomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private HomeService homeService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .build();
    }

    @Test
    @DisplayName("전체 메시지 조회")
    void totalMessageCount() throws Exception{
        // given
        final String url = "/api/v1/home/total-count";
        TotalMessageCountData totalMessageCountData = TotalMessageCountData.builder().totalHeartCount(10L).build();
        doReturn(totalMessageCountData).when(homeService).totalMessageCount();

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status",is("success")))
                .andExpect(jsonPath("$.message",is("서비스 전체 누적 메시지 수 반환합니다.")))
                .andExpect(jsonPath("$.data.totalHeartCount",is(10)))
        ;
    }
}