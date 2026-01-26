package com.nearsplit.common.security;

/**
 * packageName  : com.nearsplit.common.security
 * fileName     : TokenStatus
 * author       : user
 * date         : 2026-01-26(월)
 * description   : accessToken + refreshToken 사용하기 위한 토큰 상태값 관리
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-26(월)                user            최초 생성
 */

public enum TokenStatus {
    VALID,              // 토큰 유효
    EXPIRED,            // 토큰 만료
    INVALID_SIGNATURE,  // 서명 오류
    MALFORMED           // 형식 오류
}
