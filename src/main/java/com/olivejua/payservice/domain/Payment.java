package com.olivejua.payservice.domain;

import com.olivejua.payservice.domain.type.PaymentStatus;
import com.olivejua.payservice.error.ApplicationException;
import com.olivejua.payservice.service.dto.AgencyCancelApiResponse;
import com.olivejua.payservice.service.dto.AgencyPayApiResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Payment {
    private final Long id;
    private final User user;
    private final Long amount;
    private final PaymentStatus status;
    private final String transactionId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime approvedAt;
    private final LocalDateTime canceledAt;

    @Builder
    public Payment(Long id, User user, Long amount, PaymentStatus status, String transactionId, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime approvedAt, LocalDateTime canceledAt) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
    }

    public static Payment createWithPending(User user, long amount, LocalDateTime createdAt) {
        return Payment.builder()
                .user(user)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    public Payment approve(AgencyPayApiResponse agencyApiResponse) {
        return Payment.builder()
                .id(id)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .transactionId(agencyApiResponse.transactionId())
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .approvedAt(agencyApiResponse.approvedAt())
                .canceledAt(canceledAt)
                .build();
    }

    public Payment cancelPending() {
        if (status == PaymentStatus.CANCELLED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED", "The payment has already been canceled.");
        }

        return Payment.builder()
                .id(id)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.CANCEL_PENDING)
                .transactionId(transactionId)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .approvedAt(approvedAt)
                .canceledAt(canceledAt)
                .build();
    }

    public Payment cancel(AgencyCancelApiResponse agencyApiResponse) {
        return Payment.builder()
                .id(id)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.CANCELLED)
                .transactionId(transactionId)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .approvedAt(approvedAt)
                .canceledAt(agencyApiResponse.canceledAt())
                .build();
    }

    public void validateIfValidUser(User user) {
        if (!Objects.equals(this.user.getId(), user.getId())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "UNAUTHORIZED_CANCELLATION", "The requester is not authorized to cancel this payment.");
        }
    }

    public boolean doesNotHaveStatus(PaymentStatus status) {
        return this.status != status;
    }
}
