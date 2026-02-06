package com.nearsplit.external.juso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 프론트엔드에 반환할 주소 정보 DTO
 * juso.go.kr 응답에서 필요한 필드만 추출
 */
@Getter
@Builder
@AllArgsConstructor
public class AddressDto {

    private String roadAddr;      // 도로명주소 전체 (예: 서울특별시 강남구 역삼로 123)
    private String jibunAddr;     // 지번주소 (예: 서울특별시 강남구 역삼동 123-45)
    private String zipNo;         // 우편번호 (예: 06241)
    private String bdNm;          // 건물명 (예: 역삼타워)
    private String siNm;          // 시도명 (예: 서울특별시)
    private String sggNm;         // 시군구명 (예: 강남구)
    private String emdNm;         // 읍면동명 (예: 역삼동)

    /**
     * JusoSearchResponse.Juso → AddressDto 변환
     */
    public static AddressDto from(JusoSearchResponse.Juso juso) {
        return AddressDto.builder()
                .roadAddr(juso.getRoadAddr())
                .jibunAddr(juso.getJibunAddr())
                .zipNo(juso.getZipNo())
                .bdNm(juso.getBdNm())
                .siNm(juso.getSiNm())
                .sggNm(juso.getSggNm())
                .emdNm(juso.getEmdNm())
                .build();
    }
}
