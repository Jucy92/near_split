package com.nearsplit.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 가입 시 필요 입력 값
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(length = 20)
    private String phone;
    private Double latitude;        // 위도
    private Double longitude;       // 경도

    // 회원 수정 or 다른 곳에서 입력
    @Column(length = 500)
    private String profileImage;
    @Column(length = 500)
    private String address;
    @Column(length = 500)
    private String location;

    @Builder.Default
    private BigDecimal trustScore = BigDecimal.valueOf(5.00);

    @Builder.Default
    private boolean isVerified = false;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // ========================================
    // 정적 팩토리 메서드
    // ========================================

    /**
     * 회원 가입용 생성
     * - 신뢰 점수 5.00, 미인증 상태로 초기화
     */
    public static User createUser(String email, String encodedPassword, String name, String nickname) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .nickname(nickname)
                .trustScore(BigDecimal.valueOf(5.00))
                .isVerified(false)
                .build();
    }

    // ========================================
    // 도메인 메서드 - 프로필 수정
    // ========================================

    /**
     * 프로필 정보 수정
     * - null이 아닌 필드만 변경 (부분 업데이트)
     * - 닉네임 중복 검증은 서비스에서 수행 (Repository 필요)
     */
    public void updateProfile(String nickname, String address, String location,
                              String profileImage, String phone) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (address != null) {
            this.address = address;
        }
        if (location != null) {
            this.location = location;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }

    /**
     * 좌표 정보 업데이트
     * - 주소 변경 시 외부 API(VWorld)로 변환된 좌표를 설정
     */
    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
