package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.*;
import com.chillin.hearting.api.request.LoginTestReq;
import com.chillin.hearting.api.request.TwitterLoginReq;
import com.chillin.hearting.api.request.UpdateNicknameReq;
import com.chillin.hearting.api.request.UpdateStatusMessageReq;
import com.chillin.hearting.api.service.OAuthService;
import com.chillin.hearting.api.service.UserService;
import com.chillin.hearting.db.domain.User;
import com.google.gson.Gson;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private OAuthService oAuthService;

    private MockMvc mockMvc;

    private User user = User.builder().id("userId").nickname("nickname").build();

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
    }

    @Test
    @DisplayName("소셜 로그인")
    void socialLogin() throws Exception {
        // given
        final String url = "/api/v1/auth/guests/social/kakao";

        // mocking
        doReturn(mock(SocialLoginData.class)).when(oAuthService).socialLogin(anyString(),anyString(),any(),any());

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("code","code")
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("소셜 로그인 성공")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("트위터 로그인 성공")
    void twitterLogin() throws Exception {
        // given
        final String url = "/api/v1/auth/guests/twitter/redirect-url";

        // mocking
        doReturn(mock(TwitterRedirectData.class)).when(oAuthService).getTwitterRequestToken();

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("트위터 로그인 redirect URL 얻어오기 성공!")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("트위터 유저 정보 받아오기")
    void getTwitterUserInfo() throws Exception {
        // given
        final String url = "/api/v1/auth/guests/twitter/user-info";

        // mocking
        doReturn(mock(SocialLoginData.class)).when(oAuthService).getTwitterUserInfo(any(HttpServletRequest.class),any(HttpServletResponse.class),anyString(),anyString(),eq("twitter"));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new TwitterLoginReq("","")))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("트위터 로그인 성공!!")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("관리자 계정 로그인")
    void adminLogin() throws Exception {
        // given
        final String url = "/api/v1/auth/guests/admin/login";

        // mocking
        doReturn(mock(SocialLoginData.class)).when(userService).adminLogin(any(LoginTestReq.class),any(HttpServletRequest.class),any(HttpServletResponse.class));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new LoginTestReq("id")))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("admin 로그인 성공")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("닉네임 수정")
    void updateNickname() throws Exception {
        // given
        final String url = "/api/v1/auth/users/nickname";

        // mocking
        doReturn(mock(UpdateNicknameData.class)).when(userService).updateNickname(anyString(),anyString());
        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new UpdateNicknameReq("nickname")))
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("닉네임 변경 성공")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("상태메시지 수정")
    void updateStatusMessage() throws Exception {
        // given
        final String url = "/api/v1/auth/users/status-message";

        // mocking
        doReturn(mock(UpdateStatusMessageData.class)).when(userService).updateStatusMessage(eq(user.getId()),anyString());
        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new UpdateStatusMessageReq("status message")))
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("상태메시지 변경 성공")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("로그아웃")
    void logoutUser() throws Exception {
        // given
        final String url = "/api/v1/auth/users/logout";

        // mocking
        doNothing().when(userService).deleteRefreshToken(eq(user.getId()),any(HttpServletRequest.class),any(HttpServletResponse.class));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("로그아웃 성공")))
        ;
    }

    @Test
    @DisplayName("하트판 정보 조회")
    void getBoardOwnerInformation() throws Exception {
        // given
        final String url = "/api/v1/auth/guests/"+user.getId();

        // mocking
        doReturn(mock(HeartBoardOwnerData.class)).when(userService).getBoardOwnerInformation(eq(user.getId()));
        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("하트판 주인 정보 반환합니다.")))
                .andExpect(jsonPath("data").exists())
        ;
    }

    @Test
    @DisplayName("엑세스 토큰 재발급")
    void reissueAccessToken() throws Exception {
        // given
        final String url = "/api/v1/auth/users/access-token";

        // mocking
        doReturn(mock(ReissuedAccessTokenData.class)).when(userService).reissueAccessToken(any(HttpServletRequest.class),any(HttpServletResponse.class));
        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("access token 재발급 성공")))
                .andExpect(jsonPath("data").exists())
        ;
    }
}
