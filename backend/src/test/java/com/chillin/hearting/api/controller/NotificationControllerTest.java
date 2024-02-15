package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.NotificationListData;
import com.chillin.hearting.api.service.NotificationService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .build();
    }

    @Test
    @DisplayName("알림 조회")
    void getNotifications() throws Exception {
        // given
        final String url = "/api/v1/notifications";
        User user = User.builder().id("id").build();

        // mocking
        doReturn(mock(NotificationListData.class)).when(notificationService).getNotifications(eq(user.getId()));

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
                .andExpect(jsonPath("$.message", is("유저의 알림 조회에 성공하였습니다.")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("알림 조회 실패 - 비로그인 상태")
    void getNotificationsFailNullUser() throws Exception {
        // given
        final String url = "/api/v1/notifications";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("알림 읽기")
    void readNotification() throws Exception {
        // given
        final String url = "/api/v1/notifications/1";
        User user = User.builder().id("id").build();

        // mocking
        doReturn(1L).when(notificationService).readNotification(eq(1L));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("해당 알림이 성공적으로 읽음처리 되었습니다.")))
        ;
    }

    @Test
    @DisplayName("알림 읽기 실패 - 비로그인 상태")
    void readNotificationFailNullUser() throws Exception {
        // given
        final String url = "/api/v1/notifications/1";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isUnauthorized());
    }
}