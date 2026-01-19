package com.nearsplit.common.exception;

import lombok.Getter;

/**
 * packageName  : com.nearsplit.common.exception
 * fileName     : BusinessException
 * author       : user
 * date         : 2026-01-19(월)
 * description   : 서비스 화면에서 예외 발생 시 ErrorCode(코드,내용)에 정의 해놓은 예외 코드 사용하기 위해 추가
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-19(월)                user            최초 생성
 */

@Getter
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
