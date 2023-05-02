package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.HeartBoardOwnerData;
import com.chillin.hearting.api.data.ReissuedAccessTokenData;
import com.chillin.hearting.api.data.UpdateNicknameData;
import com.chillin.hearting.api.data.UpdateStatusMessageData;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.repository.UserRepository;
import com.chillin.hearting.exception.UnAuthorizedException;
import com.chillin.hearting.exception.UserNotFoundException;
import com.chillin.hearting.jwt.AuthToken;
import com.chillin.hearting.jwt.AuthTokenProvider;
import com.chillin.hearting.oauth.domain.AppProperties;
import com.chillin.hearting.oauth.domain.PrincipalDetails;
import com.chillin.hearting.util.CookieUtil;
import com.chillin.hearting.util.HeaderUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String SUCCESS = "success";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;


    @Value("${app.auth.refresh-token-expiry}")
    private long refreshTokenExpiry;


    @Transactional
    public UpdateNicknameData updateNickname(String userId, String nickname) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.updateNickname(nickname);

        return UpdateNicknameData.builder()
                .nickname(user.getNickname())
                .build();

    }

    @Transactional
    public UpdateStatusMessageData updateStatusMessage(String userId, String statusMessage) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.updateStatusMessage(statusMessage);

        return UpdateStatusMessageData.builder()
                .statusMessage(user.getStatusMessage())
                .build();

    }

    @Transactional
    public void deleteRefreshToken(String userId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.deleteRefreshToken();

        userRepository.save(user);

        CookieUtil.deleteCookie(httpServletRequest, httpServletResponse, REFRESH_TOKEN);

    }

    // 하트판 주인 정보 조회
    public HeartBoardOwnerData getBoardOwnerInformation(String userId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return HeartBoardOwnerData.builder()
                .nickname(user.getNickname())
                .statusMessage(user.getStatusMessage())
                .messageTotal(user.getMessageTotal())
                .build();
    }

    // access token 재발급
    @Transactional
    public ReissuedAccessTokenData reissueAccessToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        String headerAccessToken = HeaderUtil.getAccessToken(httpServletRequest);

        AuthToken authHeaderAccessToken = tokenProvider.convertAuthToken(headerAccessToken);
        Authentication authentication = tokenProvider.getExpiredUser(authHeaderAccessToken);

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

//        User user = userRepository.findById(principalDetails.getUser().getId()).orElseThrow(UserNotFoundException::new);
        User user = principalDetails.getUser();
        String refreshToken = CookieUtil.getCookie(httpServletRequest, REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || !refreshToken.equals(user.getRefreshToken())) {
            deleteRefreshToken(user.getId(), httpServletRequest, httpServletResponse);
            throw new UnAuthorizedException("DB에 저장되어 있는 refreshToken과 다릅니다. 다시 로그인 해주세요.");
        }

        log.debug("쿠키에 담긴 refreshToken : {}", refreshToken);

        AuthToken authTokenRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        Claims refreshClaims = authTokenRefreshToken.getExpiredTokenClaims();

        long expirationTime = refreshClaims.get("exp", Long.class); // "exp" 필드 값을 가져옵니다.
        long currentTime = System.currentTimeMillis() / 1000; // 현재 시간을 초 단위로 가져옵니다.

        if (expirationTime < currentTime || user.getRefreshToken() == null) {
            log.debug("유효하지 않은 refresh token 입니다.");
            deleteRefreshToken(user.getId(), httpServletRequest, httpServletResponse);

            throw new UnAuthorizedException("유효하지 않은 refresh token 입니다.");
        }


        AuthToken accessToken = makeAccessToken(user.getId());

        log.debug("정상적으로 액세스토큰 재발급!!!");


        return ReissuedAccessTokenData.builder().accessToken(accessToken.getToken()).build();
    }

    public AuthToken makeAccessToken(String userId) {

        Date now = new Date();

        return tokenProvider.createAuthToken(
                userId,
                ROLE,
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );
    }

    public AuthToken makeRefreshToken() {

        Date now = new Date();

        return tokenProvider.createAuthToken(new Date(now.getTime() + refreshTokenExpiry));
    }


}
