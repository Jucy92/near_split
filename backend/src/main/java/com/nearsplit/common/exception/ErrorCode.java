package com.nearsplit.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * packageName  : com.nearsplit.common.exception
 * fileName     : ErrorCode
 * author       : user
 * date         : 2026-01-17(토)
 * description   : 예외 처리에서 사용 할 에러 코드 정의
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-17(토)                user            최초 생성
 */
@Getter
public enum ErrorCode {
    // 공통 에러 (C: Common)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN,"C003","권한이 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"C999", "서버 오류가 발생했습니다"),

    // 사용자 에러 (U: User)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 회원입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다"),

    // 소분 그룹 에러 (G: Group)
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "존재하지 않는 그룹입니다"),
    GROUP_FULL(HttpStatus.BAD_REQUEST, "G002", "그룹이 가득 찼습니다"),
    GROUP_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "G003", "모집이 마감된 그룹입니다"),
    NOT_GROUP_OWNER(HttpStatus.FORBIDDEN, "G004", "그룹 소유자만 가능합니다"),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "G005", "이미 참여한 그룹입니다"),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "G006", "참여 신청 내역이 없습니다"),
    INVALID_PARTICIPANT_STATUS(HttpStatus.BAD_REQUEST, "G007", "유효하지 않은 참여 상태입니다"),
    CANNOT_MODIFY_WITH_PARTICIPANTS(HttpStatus.BAD_REQUEST, "G008", "참여자가 있는 경우 수정이 불가합니다"),
    CANNOT_DELETE_WITH_PARTICIPANTS(HttpStatus.BAD_REQUEST, "G009", "참여자가 있는 경우 삭제가 불가합니다"),
    INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "G010", "마감일은 내일 이후로 설정해야 합니다");
    
    // 상품 에러

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
