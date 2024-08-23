package com.olivejua.payservice.database.entity;

import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.type.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public static PaymentEntity from(Payment payment) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.id = payment.getId();
        paymentEntity.user = UserEntity.from(payment.getUser());
        paymentEntity.amount = payment.getAmount();
        paymentEntity.status = payment.getStatus();
        paymentEntity.transactionId = payment.getTransactionId();
        paymentEntity.createdAt = payment.getCreatedAt();
        paymentEntity.updatedAt = payment.getUpdatedAt();
        paymentEntity.canceledAt = payment.getCanceledAt();

        return paymentEntity;
    }
}
