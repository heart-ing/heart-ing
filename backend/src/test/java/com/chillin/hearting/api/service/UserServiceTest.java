package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.*;
import com.chillin.hearting.api.request.LoginTestReq;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.UserRepository;
import com.chillin.hearting.exception.UnAuthorizedException;
import com.chillin.hearting.jwt.AuthToken;
import com.chillin.hearting.jwt.AuthTokenProvider;
import com.chillin.hearting.oauth.domain.AppProperties;
import com.chillin.hearting.oauth.domain.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends AbstractTestData {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthTokenProvider tokenProvider;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private AppProperties appProperties;

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    ValueOperations mockValueOperations = mock(ValueOperations.class);

    final String COOKIE_REFRESH_TOKEN_NAME = "refreshToken";
    final String COOKIE_REFRESH_TOKEN_VALUE = "refresh token";
    final String COOKIE_REFRESH_TOKEN_BAD_VALUE = "bad token";
    final String SAVED_REFRESH_TOKEN = "refresh token";
    final String REISSUED_ACCESS_TOKEN = "new access token";

    final Cookie[] cookies = new Cookie[]{new Cookie(COOKIE_REFRESH_TOKEN_NAME,COOKIE_REFRESH_TOKEN_VALUE)};
    Cookie[] badCookies = new Cookie[]{new Cookie(COOKIE_REFRESH_TOKEN_NAME,COOKIE_REFRESH_TOKEN_BAD_VALUE)};
    Cookie[] noCookies = new Cookie[]{};

    final String VALID_ACCESS_TOKEN = "aaa";
    final String VALID_HEADER_VALUE = "Bearer: "+VALID_ACCESS_TOKEN;

    final PrincipalDetails principalDetails = new PrincipalDetails(createUser("id"),null);
    final Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, null);


    @Test
    @DisplayName("닉네입 수정")
    void updateNickname() {
        // given
        String newNickname = "newNickname";
        User user = createUser("id");
        doReturn(Optional.of(user)).when(userRepository).findById(anyString());

        // when
        final UpdateNicknameData updateNicknameData = userService.updateNickname("id", newNickname);

        // then
        assertThat(updateNicknameData.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("상태메시지 수정")
    void updateStatusMessage() {
        // given
        String newStatus = "New Status";
        User user = createUser("id");
        doReturn(Optional.of(user)).when(userRepository).findById(anyString());

        // when
        UpdateStatusMessageData UpdateStatusMessageData = userService.updateStatusMessage("userId", newStatus);

        // then
        assertThat(UpdateStatusMessageData.getStatusMessage()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("리프레시 토큰 삭제")
    void deleteRefreshToken() {
        // given
        User user = createUser("id");
        doReturn(Optional.of(user)).when(userRepository).findById(anyString());
        doReturn(user).when(userRepository).save(any(User.class));

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        String COOKIE_NAME = "cookie";
        String COOKIE_VALUE = "value";

        Cookie cookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);
        cookie.setMaxAge(100000);
        Cookie[] cookies = new Cookie[]{cookie};

        doReturn(cookies).when(mockRequest).getCookies();

        // when
        userService.deleteRefreshToken("id", mockRequest, mockResponse);

        // then
        assertThat(cookie).extracting(c -> c.getMaxAge() == 0);
    }

    @Test
    @DisplayName("게시판 주인 정보 조회")
    void getBoardOwnerInformation() {
        // given
        final String USER_STATUS_MESSAGE = "status message";
        User user = createUser("id");
        user.updateStatusMessage(USER_STATUS_MESSAGE,null);
        doReturn(Optional.of(user)).when(userRepository).findById(anyString());

        // when
        HeartBoardOwnerData result = userService.getBoardOwnerInformation("id");

        // then
        assertThat(result).extracting(dto ->
                dto.getNickname().equals(user.getNickname()) &&
                        dto.getStatusMessage().equals(user.getStatusMessage()) &&
                        dto.getMessageTotal().equals(user.getMessageTotal()));
    }

    @Test
    @DisplayName("Access Token 재발급")
    void reissueAccessToken() {
        // given
        final AuthToken mockAuthToken = mock(AuthToken.class);

        doReturn(VALID_HEADER_VALUE).when(mockRequest).getHeader(anyString());
        doReturn(cookies).when(mockRequest).getCookies();

        doReturn(mockValueOperations).when(redisTemplate).opsForValue();
        doReturn(SAVED_REFRESH_TOKEN).when(mockValueOperations).get(anyString());

        UserService spyUserService = spy(userService);
        doReturn(mockAuthToken).when(spyUserService).makeAccessToken(anyString());
        doReturn(REISSUED_ACCESS_TOKEN).when(mockAuthToken).getToken();

        when(tokenProvider.convertAuthToken(any())).thenReturn(mock(AuthToken.class));
        when(tokenProvider.getExpiredUser(any(AuthToken.class))).thenReturn(authentication);

        // when
        ReissuedAccessTokenData result = spyUserService.reissueAccessToken(mockRequest, mockResponse);

        // then
        assertThat(result).extracting(r -> r.getAccessToken().equals(REISSUED_ACCESS_TOKEN));
    }

    @Test
    @DisplayName("ACCESS TOKEN 재발급 실패 - COOKIE의 REFRESH TOKEN 이슈")
    void reissueAccessTokenWithInvalidCookie() {
        // given
        UserService spyUserService = spy(userService);
        doNothing().when(spyUserService).deleteUserToken(anyString());
        doNothing().when(spyUserService).deleteCookieRefreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

        doReturn(mockValueOperations).when(redisTemplate).opsForValue();
        doReturn(SAVED_REFRESH_TOKEN).when(mockValueOperations).get(anyString());

        doReturn(badCookies).when(mockRequest).getCookies();

        when(tokenProvider.convertAuthToken(any())).thenReturn(mock(AuthToken.class));
        when(tokenProvider.getExpiredUser(any(AuthToken.class))).thenReturn(authentication);

        // when
        try {
            spyUserService.reissueAccessToken(mockRequest, mockResponse);
        } catch (Exception e) {
            // then
            assertThat(e).isInstanceOf(UnAuthorizedException.class);
        }

        // when
        HttpServletRequest noCookieMockRequest = mock(HttpServletRequest.class);
        doReturn(noCookies).when(mockRequest).getCookies();

        try {
            spyUserService.reissueAccessToken(noCookieMockRequest, mockResponse);
        } catch (Exception e) {
            // then
            assertThat(e).isInstanceOf(UnAuthorizedException.class);
        }
    }

    @Test
    @DisplayName("ACCESS TOKEN 재발급 실패 - REFRESH TOKEN 만료")
    void reissueAccessTokenExpired() {
        // given
        UserService spyUserService = spy(userService);
        doNothing().when(spyUserService).deleteUserToken(anyString());
        doNothing().when(spyUserService).deleteCookieRefreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

        doReturn(mockValueOperations).when(redisTemplate).opsForValue();
        doThrow(NullPointerException.class).when(mockValueOperations).get(anyString());

        doReturn(cookies).when(mockRequest).getCookies();

        when(tokenProvider.convertAuthToken(any())).thenReturn(mock(AuthToken.class));
        when(tokenProvider.getExpiredUser(any(AuthToken.class))).thenReturn(authentication);

        // when
        try {
            spyUserService.reissueAccessToken(mockRequest, mockResponse);
        } catch (Exception e) {
            // then
            assertThat(e).isInstanceOf(UnAuthorizedException.class);
        }
    }

    @Test
    @DisplayName("ACCESS TOKEN 발급")
    void makeAccessToken() {
        // given
        final String userId = "userId";
        final Long tokenExpiry = 1L;
        doReturn(mock(AuthToken.class)).when(tokenProvider).createAuthToken(eq(userId),anyString(),any(Date.class));
        AppProperties.Auth auth = mock(AppProperties.Auth.class);
        doReturn(auth).when(appProperties).getAuth();
        doReturn(tokenExpiry).when(auth).getTokenExpiry();

        // when
        AuthToken authToken = userService.makeAccessToken(userId);

        // then
        assertThat(authToken).isNotNull();
    }

    @Test
    @DisplayName("REFRESH TOKEN 발급")
    void makeRefreshToken() {
        // given
        doReturn(mock(AuthToken.class)).when(tokenProvider).createAuthToken(any(Date.class));

        // when
        AuthToken authToken = userService.makeRefreshToken();

        // then
        assertThat(authToken).isNotNull();
    }

    @Test
    @DisplayName("ID로 유저 조회")
    void findById() {
        // given
        final String userId = "userId";
        User user = createUser(userId);
        doReturn(Optional.of(user)).when(userRepository).findById(userId);

        // when
        User findUser = userService.findById(userId);

        // then
        assertThat(findUser.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("ADMIN 로그인")
    void adminLogin() {
        // given
        final String adminId = "adminID";
        final Long tokenExpiry = 1L;
        LoginTestReq loginTestReq = new LoginTestReq(adminId);
        User admin = createUser(adminId);
        AuthToken mockAccessToken = mock(AuthToken.class);
        AuthToken mockRefreshToken = mock(AuthToken.class);

        UserService spyService = spy(userService);

        doReturn(admin).when(spyService).findById(eq(adminId));
        AppProperties.Auth auth = mock(AppProperties.Auth.class);
        doReturn(auth).when(appProperties).getAuth();
        doReturn(tokenExpiry).when(auth).getTokenExpiry();
        doReturn(mockAccessToken).when(tokenProvider).createAuthToken(eq(adminId),anyString(),any(Date.class));
        doReturn(VALID_ACCESS_TOKEN).when(mockAccessToken).getToken();
        doReturn(mockRefreshToken).when(spyService).makeRefreshToken();
        doReturn(mockValueOperations).when(redisTemplate).opsForValue();
        doNothing().when(mockValueOperations).set(anyString(),anyString(),anyLong(),any(TimeUnit.class));
        doReturn(cookies).when(mockRequest).getCookies();
        doNothing().when(mockResponse).addCookie(any(Cookie.class));

        // when
        SocialLoginData socialLoginData = spyService.adminLogin(loginTestReq,mockRequest, mockResponse);

        // then
        assertThat(socialLoginData.getUserId()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("토큰 제거")
    void deleteUserToken() {
        // given
        doReturn(mockValueOperations).when(redisTemplate).opsForValue();
        RedisOperations mockRedisOperations = mock(RedisOperations.class);
        doReturn(mockRedisOperations).when(mockValueOperations).getOperations();
        doReturn(true).when(mockRedisOperations).delete(anyString());

        // when
        userService.deleteUserToken("userID");

        // then
        verify(mockRedisOperations,times(1)).delete(anyString());
    }

    @Test
    @DisplayName("Cookie에서 REFRESH TOKEN 삭제")
    void deleteCookieRefreshToken() {
        // given
        doReturn(cookies).when(mockRequest).getCookies();
        doNothing().when(mockResponse).addCookie(any(Cookie.class));

        // when
        userService.deleteCookieRefreshToken(mockRequest,mockResponse);

        // then
        assertThat(cookies).extracting(c -> c.getMaxAge() == 0);
    }

    @Test
    @DisplayName("유저 저장")
    void save() {
        // given
        final String userId = "id";
        User user = createUser(userId);
        doReturn(user).when(userRepository).save(user);

        // when
        User savedUser = userService.save(user);

        // then
        assertThat(savedUser.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("모든 유저 조회")
    void findAll() {
        // given
        User user1 = createUser("1");
        User user2 = createUser("2");
        List<User> userList = List.of(user1, user2);
        doReturn(userList).when(userRepository).findAll();

        // when
        List<User> findUserList = userService.findAll();

        // then
        assertThat(findUserList.size()).isEqualTo(userList.size());
    }
}
