package com.nearsplit.external.vworld.dto;


import lombok.Getter;

/**
 * packageName  : com.nearsplit.external.vworld.dto
 * fileName     : VWorldGeocodingResponse
 * author       : user
 * date         : 2026-02-04(수)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-04(수)                user            최초 생성
 */
@Getter
public class VWorldGeocodingResponse {
    private Response response;

    @Getter
    public static class Response {
        private String status;
        private Result result;
    }

    @Getter
    public static class Result {
        private Point point;
    }

    @Getter
    public static class Point {
        private String x;       // 경도 (longitude)
        private String y;       // 위도 (latitude)
    }
}
