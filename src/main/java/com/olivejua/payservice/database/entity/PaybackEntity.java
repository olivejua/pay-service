package com.olivejua.payservice.database.entity;

import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.type.PaybackStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "paybacks")
public class PaybackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private PaybackPolicyEntity policy;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaybackStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public static PaybackEntity from(Payback payback) {
        PaybackEntity paybackEntity = new PaybackEntity();
        paybackEntity.id = payback.getId();
        paybackEntity.policy = PaybackPolicyEntity.from(payback.getPolicy());
        paybackEntity.payment = PaymentEntity.from(payback.getPayment());
        paybackEntity.amount = payback.getAmount();
        paybackEntity.status = payback.getStatus();
        paybackEntity.createdAt = payback.getCreatedAt();
        paybackEntity.updatedAt = payback.getUpdatedAt();
        paybackEntity.canceledAt = payback.getCanceledAt();

        return paybackEntity;
    }

    public Payback toModel() {
        return Payback.builder()
                .id(id)
                .policy(policy.toModel())
                .payment(payment.toModel())
                .amount(amount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .canceledAt(canceledAt)
                .build();
    }
}
