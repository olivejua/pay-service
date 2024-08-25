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

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "max_payback_amount")
    private BigDecimal maxPaybackAmount;

    @Column(name = "min_purchase_amount")
    private BigDecimal minPurchaseAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static PaybackPolicyEntity from(PaybackPolicy paybackPolicy) {
        PaybackPolicyEntity paybackPolicyEntity = new PaybackPolicyEntity();
        paybackPolicyEntity.id = paybackPolicy.getId();
        paybackPolicyEntity.startDate = paybackPolicy.getStartDate();
        paybackPolicyEntity.endDate = paybackPolicy.getEndDate();
        paybackPolicyEntity.isActive = paybackPolicy.isActive();
        paybackPolicyEntity.rate = paybackPolicy.getRate();
        paybackPolicyEntity.maxPaybackAmount = paybackPolicy.getMaxPaybackAmount();
        paybackPolicyEntity.minPurchaseAmount = paybackPolicy.getMinPurchaseAmount();
        paybackPolicyEntity.createdAt = paybackPolicy.getCreatedAt();
        paybackPolicyEntity.updatedAt = paybackPolicy.getUpdatedAt();

        return paybackPolicyEntity;
    }

    public PaybackPolicy toModel() {
        return PaybackPolicy.builder()
                .id(id)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(isActive)
                .rate(rate)
                .maxPaybackAmount(maxPaybackAmount)
                .minPurchaseAmount(minPurchaseAmount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
