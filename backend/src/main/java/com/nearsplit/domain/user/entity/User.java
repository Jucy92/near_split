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
@Getter @Setter @Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED)    // Setter 사용안하면 이거 활성화 시켜도 됨 -> UserRepositoryTest 에서 Set 사용
@AllArgsConstructor
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

}


