package com.chillin.hearting.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class HeaderUtilTest {

    @Test
    @DisplayName("private 생성자 에러")
    public void testPrivateConstructor() throws Exception {
        Constructor<HeaderUtil> constructor = HeaderUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("엑세스 토큰 가져오기")
    void getAccessToken() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String headerToken = "Bearer accessToken";
        doReturn(headerToken).when(request).getHeader(anyString());

        // when
        String token = HeaderUtil.getAccessToken(request);

        // then
        assertThat(token).isEqualTo("accessToken");
    }

}