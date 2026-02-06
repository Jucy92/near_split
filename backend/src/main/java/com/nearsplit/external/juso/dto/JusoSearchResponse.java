package com.nearsplit.external.juso.dto;

import lombok.Getter;
import java.util.List;

/**
 * juso.go.kr 검색 API 응답 매핑 DTO
 *
 * API 응답 구조:
 * {
 *   "results": {
 *     "common": { "errorCode": "0", "errorMessage": "정상", "totalCount": "10" },
 *     "juso": [ { "roadAddr": "...", "jibunAddr": "...", ... } ]
 *   }
 * }
 */
@Getter
public class JusoSearchResponse {

    private Results results;

    @Getter
    public static class Results {
        private Common common;
        private List<Juso> juso;
    }

    @Getter
    public static class Common {
        private String errorCode;      // "0" = 정상
        private String errorMessage;   // 에러 메시지
        private String totalCount;     // 총 검색 결과 수
    }

    @Getter
    public static class Juso {
        private String roadAddr;       // 도로명주소 전체
        private String roadAddrPart1;  // 도로명주소 (건물번호까지)
        private String roadAddrPart2;  // 참고항목 (동, 층 등)
        private String jibunAddr;      // 지번주소
        private String zipNo;          // 우편번호
        private String bdNm;           // 건물명
        private String siNm;           // 시도명
        private String sggNm;          // 시군구명
        private String emdNm;          // 읍면동명
    }

    /**
     * API 호출 성공 여부 확인
     */
    public boolean isSuccess() {
        return results != null
            && results.getCommon() != null
            && "0".equals(results.getCommon().getErrorCode());
    }

    /**
     * 에러 메시지 반환
     */
    public String getErrorMessage() {
        if (results != null && results.getCommon() != null) {
            return results.getCommon().getErrorMessage();
        }
        return "알 수 없는 오류";
    }
}
