package com.nearsplit.external.juso.controller;

import com.nearsplit.external.juso.dto.AddressDto;
import com.nearsplit.external.juso.service.JusoSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 주소 검색 API 컨트롤러
 *
 * 프론트엔드에서 주소 검색 시 이 API를 호출
 * 백엔드가 juso.go.kr에 요청 후 결과 반환 (CORS 우회)
 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final JusoSearchService jusoSearchService;

    /**
     * 주소 검색 API
     *
     * @param keyword 검색 키워드 (예: "역삼동", "테헤란로 123")
     * @return 주소 목록
     *
     * 사용 예시:
     * GET /api/address/search?keyword=역삼동
     *
     * 응답 예시:
     * [
     *   {
     *     "roadAddr": "서울특별시 강남구 역삼로 123",
     *     "jibunAddr": "서울특별시 강남구 역삼동 123-45",
     *     "zipNo": "06241",
     *     "bdNm": "역삼타워",
     *     "siNm": "서울특별시",
     *     "sggNm": "강남구",
     *     "emdNm": "역삼동"
     *   }
     * ]
     */
    @GetMapping("/search")
    public ResponseEntity<List<AddressDto>> searchAddress(
            @RequestParam String keyword
    ) {
        List<AddressDto> results = jusoSearchService.search(keyword);
        return ResponseEntity.ok(results);
    }
}
