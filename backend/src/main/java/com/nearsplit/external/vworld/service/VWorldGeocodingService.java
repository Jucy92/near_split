package com.nearsplit.external.vworld.service;

import com.nearsplit.external.vworld.client.VWorldApiClient;
import com.nearsplit.external.vworld.dto.Coordinate;
import com.nearsplit.external.vworld.dto.VWorldGeocodingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * packageName  : com.nearsplit.external.vworld.service
 * fileName     : VWorldGeocodingService
 * author       : user
 * date         : 2026-02-04(수)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-04(수)                user            최초 생성
 */

@Service
@RequiredArgsConstructor
public class VWorldGeocodingService {
    private final VWorldApiClient client;

    public Coordinate getCoordinate(String address) {
        // 1. API 호출
        VWorldGeocodingResponse response = client.getCoordinate(address);

        // 2. 응답 검증
        if (!"OK".equals(response.getResponse().getStatus())) {
            throw new RuntimeException("주소를 찾을 수 없습니다: " + address);
        }

        // 3. 좌표 추출 및 변환
        var point = response.getResponse().getResult().getPoint();
        double longitude = Double.parseDouble(point.getX());  // 경도
        double latitude = Double.parseDouble(point.getY());   // 위도

        // 4. Coordinate 객체 반환
        return new Coordinate(latitude, longitude);
    }
}
