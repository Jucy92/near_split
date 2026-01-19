package com.nearsplit.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName  : com.nearsplit.common.dto
 * fileName     : ApiResponse
 * author       : user
 * date         : 2026-01-19(월)
 * description   : 응답 처리 모듈화 (성공!=실패 응답 구조가 다름에서 시작)
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-19(월)                user            최초 생성
 */

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;

    // 성공 응답 (데이터만)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    // 성공 응답 (데이터 + 메세지)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    // 성공 응답 (메세지만, 데이터 없음)
    public static <T> ApiResponse<T> successWithMessage(String message) {
        return new ApiResponse<>(true, null, message);
    }
    



}
