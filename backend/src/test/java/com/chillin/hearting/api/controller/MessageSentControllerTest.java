package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.SentMessageData;
import com.chillin.hearting.api.data.SentMessageListData;
import com.chillin.hearting.api.service.MessageSentService;
import com.chillin.hearting.db.domain.User;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MessageSentControllerTest {

    @InjectMocks
    private MessageSentController messageSentController;
    @Mock
    private MessageSentService messageSentService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageSentController)
                .build();
    }

    @Test
    @DisplayName("보낸 메시지 조회")
    void getSentMessages() throws Exception {
        // given
        final String url = "/api/v1/messages/sent";
        User user = User.builder().id("senderId").build();
        SentMessageListData sentMessageListData = SentMessageListData.builder().build();
        doReturn(sentMessageListData).when(messageSentService).getSentMessages(eq("senderId"));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("보낸 메시지 리스트 조회를 성공했습니다.")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("보낸 메시지 상세 조회")
    void getSentMessageDetail() throws Exception {
        // given
        final String url = "/api/v1/messages/sent/1";
        User user = User.builder().id("senderId").build();
        doReturn(mock(SentMessageData.class)).when(messageSentService).getSentMessageDetail(eq("senderId"),eq(1L));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("보낸 메시지 상세 조회를 성공했습니다.")))
                .andExpect(jsonPath("data").exists())
        ;
    }
}