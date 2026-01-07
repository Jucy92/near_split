package com.nearsplit.domain.split_group.entity;

/**
 * packageName  : com.nearsplit.domain.split_group.entity
 * fileName     : SplitGroupStatus
 * author       : user
 * date         : 2026-01-07(수)
 * description   : 그룹 상태 코드 관리
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-07(수)         jcy            최초 생성
 */

public enum SplitGroupStatus {
 RECRUITING,  // 모집 중
 FULL,        // 모집 완료
 CLOSED,      // 거래 종료
 COMPLETED,   // 완료
 CANCELLED    // 취소
}
