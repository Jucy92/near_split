package com.nearsplit.external.vworld.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * packageName  : com.nearsplit.external.vworld.dto
 * fileName     : Coordinate
 * author       : user
 * date         : 2026-02-04(수)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-04(수)                user            최초 생성
 */

@Getter
@AllArgsConstructor
public class Coordinate {
    private double latitude;    // 위도 (x)
    private double longitude;   // 경도 (y)
}
