package com.nearsplit.domain.split_group.entity;

import com.nearsplit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Builder
@RequiredArgsConstructor
@Table(name = "split_group")
@AllArgsConstructor
public class SplitGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User host_user_id;

    //@OneToMany
    @JoinColumn(name = "product_id", nullable = false)
    private Long product_id;
    @Column(length = 50)
    private String title;
    private BigDecimal totalPrice;
    private int maxParticipants;
    private int currentParticipants;
    private String pickupLocation;
    private String pickupLocationGeo;
    private LocalDate pickupDate;
    private LocalDate closedAt;
    private String status;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;





}
