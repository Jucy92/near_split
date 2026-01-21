package com.nearsplit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * packageName  : com.nearsplit.config
 * fileName     : WebSocketConfig
 * author       : user
 * date         : 2026-01-21(수)
 * description   : webSocket 설정 파일
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-21(수)                user            최초 생성
 */

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker   // 1. WebSocket 핸들러 등록 2. STOMP 프로토콜 지원 활성화 3. 메시지 브로커 인프라 구축 4. @MessageMapping 어노테이션 인식
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // "/topic"으로 시작하는 메시지는 메시지 브로커가 처리 (브로드캐스트)
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 메시지를 보낼 때 "/app"으로 시작하는 경로 사용
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)   // jwt 토큰 가져와서 userId webSocket 세션에 저장
                .setAllowedOriginPatterns("*")  // CORS 설정 (개발 환경) => 이후 운영에서는 도메인 주소(https://nearsplit.com)
                .withSockJS();  // SockJS fallback 지원
    }
}
