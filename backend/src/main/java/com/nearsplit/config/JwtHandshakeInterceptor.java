package com.nearsplit.config;

import com.nearsplit.common.security.JwtUtil;
import com.nearsplit.common.security.TokenStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * packageName  : com.nearsplit.config
 * fileName     : JwtHandshakeInterceptor
 * author       : user
 * date         : 2026-01-21(수)
 * description   : webSocket 세션에 userId 저장(메모리)
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-21(수)                user            최초 생성
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {  // HandshakeInterceptor WebSocket 연결 시 최초 1회만 실행

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {


        String token = "";
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
            }
        }
        if (token != null) {
            TokenStatus tokenStatus = jwtUtil.validToken(token);
            switch (tokenStatus) {
                case VALID:
                    Long userId = jwtUtil.getUserId(token);
                    attributes.put("userId", userId);
                    log.info("WebSocket 연결 성공 - userId: {}", userId);
                    return true;
                case EXPIRED:
                    log.warn("WebSocket 연결 거부 - 토큰 만료");
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;

                default:
                    log.warn("WebSocket 연결 거부 - 유효하지 않은 토큰: {}", tokenStatus);
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
            }
        }
        // 여기서 토큰 예외처리 해주고 싶은데.. 어차피 글로벌 예외도 생성해놔서 코드 하나 만들고 try-catch로 비즈니스 에러 터뜨려서 알려주면 되는데..

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 연결 실패", exception);
        } else {
            log.info("WebSocket 연결 완료");
        }

    }
}
