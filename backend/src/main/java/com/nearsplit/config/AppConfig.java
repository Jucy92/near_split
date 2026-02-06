package com.nearsplit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 파일: AppConfig.java
 * 설명: 애플리케이션 공통 설정
 *       - RestTemplate Bean 등록 (외부 API 호출용)
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate Bean 등록
     *
     * 사용처:
     *   - PaymentService: 토스페이먼츠 API 호출
     *   - 기타 외부 API 연동
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
