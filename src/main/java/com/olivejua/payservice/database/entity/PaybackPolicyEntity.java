package com.olivejua.payservice.database.entity;

import com.olivejua.payservice.domain.PaybackPolicy;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payback_policies")
public class PaybackPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "max_payback_amount")
    private Long maxPaybackAmount;

    @Column(name = "min_payment_amount")
    private Long minPaymentAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static PaybackPolicyEntity from(PaybackPolicy paybackPolicy) {
        PaybackPolicyEntity paybackPolicyEntity = new PaybackPolicyEntity();
        paybackPolicyEntity.id = paybackPolicy.getId();
        paybackPolicyEntity.name = paybackPolicy.getName();
        paybackPolicyEntity.startDate = paybackPolicy.getStartDate();
        paybackPolicyEntity.endDate = paybackPolicy.getEndDate();
        paybackPolicyEntity.isActive = paybackPolicy.isActive();
        paybackPolicyEntity.rate = paybackPolicy.getRate();
        paybackPolicyEntity.maxPaybackAmount = paybackPolicy.getMaxPaybackAmount();
        paybackPolicyEntity.minPaymentAmount = paybackPolicy.getMinPaymentAmount();
        paybackPolicyEntity.createdAt = paybackPolicy.getCreatedAt();
        paybackPolicyEntity.updatedAt = paybackPolicy.getUpdatedAt();

        return paybackPolicyEntity;
    }

    public PaybackPolicy toModel() {
        return PaybackPolicy.builder()
                .id(id)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(isActive)
                .rate(rate)
                .maxPaybackAmount(maxPaybackAmount)
                .minPaymentAmount(minPaymentAmount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
