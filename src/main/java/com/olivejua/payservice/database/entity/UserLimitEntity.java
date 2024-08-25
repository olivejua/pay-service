package com.olivejua.payservice.database.entity;

import com.olivejua.payservice.domain.UserLimit;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_limits")
public class UserLimitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "max_balance")
    private long maxBalance;

    @Column(name = "single_payment_limit")
    private Long singlePaymentLimit;

    @Column(name = "daily_payment_limit")
    private Long dailyPaymentLimit;

    @Column(name = "monthly_payment_limit")
    private Long monthlyPaymentLimit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static UserLimitEntity from(UserLimit userLimit) {
        UserLimitEntity userLimitEntity = new UserLimitEntity();
        userLimitEntity.id = userLimit.getId();
        userLimitEntity.user = UserEntity.from(userLimit.getUser());
        userLimitEntity.maxBalance = userLimit.getMaxBalance();
        userLimitEntity.singlePaymentLimit = userLimit.getSinglePaymentLimit();
        userLimitEntity.dailyPaymentLimit = userLimit.getDailyPaymentLimit();
        userLimitEntity.monthlyPaymentLimit = userLimit.getMonthlyPaymentLimit();
        userLimitEntity.createdAt = userLimit.getCreatedAt();
        userLimitEntity.updatedAt = userLimit.getUpdatedAt();

        return userLimitEntity;
    }

    public UserLimit toModel() {
        return UserLimit.builder()
                .id(id)
                .user(user.toModel())
                .maxBalance(maxBalance)
                .singlePaymentLimit(singlePaymentLimit)
                .dailyPaymentLimit(dailyPaymentLimit)
                .monthlyPaymentLimit(monthlyPaymentLimit)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
