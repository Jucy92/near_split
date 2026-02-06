package com.nearsplit.external.juso.client;

import com.nearsplit.external.juso.dto.JusoSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * juso.go.kr 도로명주소 검색 API 클라이언트
 *
 * API 문서: https://business.juso.go.kr/addrlink/addrLinkApi.do
 */
@Component
public class JusoApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public JusoApiClient(
            @Value("${juso.api.base-url}") String baseUrl,
            @Value("${juso.api.key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    /**
     * 주소 검색 API 호출
     *
     * @param keyword 검색 키워드 (예: "역삼동", "테헤란로 123")
     * @param currentPage 현재 페이지 (기본: 1)
     * @param countPerPage 페이지당 결과 수 (기본: 10, 최대: 100)
     * @return JusoSearchResponse 검색 결과
     */
    public JusoSearchResponse search(String keyword, int currentPage, int countPerPage) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("confmKey", apiKey)           // API 인증키
                        .queryParam("keyword", keyword)            // 검색어
                        .queryParam("currentPage", currentPage)    // 현재 페이지
                        .queryParam("countPerPage", countPerPage)  // 페이지당 결과 수
                        .queryParam("resultType", "json")          // 응답 형식
                        .build())
                .retrieve()
                .body(JusoSearchResponse.class);
    }

    /**
     * 주소 검색 (기본값 사용)
     */
    public JusoSearchResponse search(String keyword) {
        return search(keyword, 1, 10);
    }
}
