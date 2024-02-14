package com.chillin.hearting.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CookieUtilTest {

    @Test
    @DisplayName("쿠키 획득")
    void getCookie() {
        // given
        Cookie cookie = new Cookie("name", "value");
        cookie.setMaxAge(1000);
        Cookie[] cookies = {new Cookie("different_name","1"), cookie};
        HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn(cookies).when(request).getCookies();

        // when
        Cookie findCookie = CookieUtil.getCookie(request, "name").get();

        // then
        assertThat(cookie).isEqualTo(findCookie);
    }

    @Test
    @DisplayName("쿠키 획득 null")
    void getCookieNull() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn(new Cookie[]{}).when(request).getCookies();

        // when
        Optional<Cookie> findCookie = CookieUtil.getCookie(request, "name");

        // then
        assertThat(findCookie.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("쿠키 삭제")
    void deleteCookie() {
        // given
        Cookie cookie = new Cookie("name", "value");
        cookie.setMaxAge(1000);
        Cookie[] cookies = {cookie};
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        doReturn(cookies).when(request).getCookies();

        // when
        CookieUtil.deleteCookie(request, response, "name");

        // then
        assertThat(cookie.getValue()).isEqualTo("");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(0);

        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("쿠키 삭제 null")
    void deleteCookieNull() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        doReturn(null).when(request).getCookies();

        // when
        CookieUtil.deleteCookie(request, response, "name");

        // then
        verify(response, times(0)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("private 생성자 에러")
    public void testPrivateConstructor() throws Exception {
        Constructor<CookieUtil> constructor = CookieUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }
}