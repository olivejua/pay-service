package com.olivejua.payservice.domain;

import com.olivejua.payservice.domain.type.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {
    private final Long id;
    private final String name;
    private final String email;
    private final UserStatus status;
    private final long currentBalance;
    private final String accountBank;
    private final String accountNumber;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public User(Long id, String name, String email, UserStatus status, long currentBalance, String accountBank, String accountNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.currentBalance = currentBalance;
        this.accountBank = accountBank;
        this.accountNumber = accountNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean hasActiveStatus() {
        return status == UserStatus.ACTIVE;
    }
}
