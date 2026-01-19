package com.nearsplit.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * packageName  : com.nearsplit.common.exception
 * fileName     : ErrorResponse
 * author       : user
 * date         : 2026-01-17(토)
 * description   :에러 메시지 응답 형식 정의
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-17(토)                user            최초 생성
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)      // null 필드는 JSON에서 제외
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, String> errors;    // Validation 오류 상세

    // ErrorCode로부터 생성
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ErrorCode + Validation 오류로부터 생성
    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> errors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

}
