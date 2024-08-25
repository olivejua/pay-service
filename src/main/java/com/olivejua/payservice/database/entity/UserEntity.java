package com.olivejua.payservice.database.entity;

import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.type.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "current_balance")
    private Long currentBalance;

    @Column(name = "account_bank")
    private String accountBank;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static UserEntity from(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.id = user.getId();
        userEntity.name = user.getName();
        userEntity.email = user.getEmail();
        userEntity.status = user.getStatus();
        userEntity.currentBalance = user.getCurrentBalance();
        userEntity.accountBank = user.getAccountBank();
        userEntity.accountNumber = user.getAccountNumber();
        userEntity.createdAt = user.getCreatedAt();
        userEntity.updatedAt = user.getUpdatedAt();

        return userEntity;
    }

    public User toModel() {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .status(status)
                .currentBalance(currentBalance)
                .accountBank(accountBank)
                .accountNumber(accountNumber)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
