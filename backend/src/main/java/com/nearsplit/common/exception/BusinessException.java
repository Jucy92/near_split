package com.nearsplit.common.exception;

import lombok.Getter;

/**
 * packageName  : com.nearsplit.common.exception
 * fileName     : BusinessException
 * author       : user
 * date         : 2026-01-19(월)
 * description   :
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
