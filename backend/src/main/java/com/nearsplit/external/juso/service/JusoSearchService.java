package com.nearsplit.external.juso.service;

import com.nearsplit.external.juso.client.JusoApiClient;
import com.nearsplit.external.juso.dto.AddressDto;
import com.nearsplit.external.juso.dto.JusoSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주소 검색 서비스
 * juso.go.kr API를 호출하고 결과를 가공하여 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JusoSearchService {

    private final JusoApiClient jusoApiClient;

    /**
     * 주소 검색
     *
     * @param keyword 검색 키워드
     * @return 주소 목록 (AddressDto)
     */
    public List<AddressDto> search(String keyword) {
        // 검색어 유효성 검사
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // juso.go.kr API 호출
            JusoSearchResponse response = jusoApiClient.search(keyword.trim());

            // 응답 검증
            if (!response.isSuccess()) {
                log.warn("주소 검색 실패: {}", response.getErrorMessage());
                throw new RuntimeException("주소 검색 실패: " + response.getErrorMessage());
            }

            // 결과가 없으면 빈 리스트 반환
            if (response.getResults().getJuso() == null) {
                return Collections.emptyList();
            }

            // Juso → AddressDto 변환
            return response.getResults().getJuso().stream()
                    .map(AddressDto::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("주소 검색 중 오류 발생: keyword={}, error={}", keyword, e.getMessage());
            throw new RuntimeException("주소 검색 중 오류가 발생했습니다.", e);
        }
    }
}
