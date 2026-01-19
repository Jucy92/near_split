package com.nearsplit.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * packageName  : com.nearsplit.common.exception
 * fileName     : GlobalExceptionHandler
 * author       : user
 * date         : 2026-01-19(월)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-19(월)                user            최초 생성
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 우선 순위 = 위에서부터 순차적 (exception 최상단에 두면 나머지 다 먹힘)

    // 1. BusinessException 처리 (직접 던진 예외)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {} - {}", e.getErrorCode().getCode(), e.getMessage());

        ErrorResponse response = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    // 2. Validation 예외 처리 (@Valid 실패)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.info("Validation failed: {}", e.getBindingResult());

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, errors);
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus()).body(response);
    }

    // 3. IllegalArgumentException 처리(Service 에서 사용 중)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code("C001")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        //ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT);   // 위 방식과 이 방식의 결과는 같음
        return ResponseEntity.badRequest().body(response);
        //return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus()).body(response);
        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 4. 예상치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);  // 전체 스택 트레이스 로깅

        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(response);
    }
}

