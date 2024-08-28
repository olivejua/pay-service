package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.domain.type.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findAllByUserIdAndStatusAndCreatedAtBetween(Long userId, PaymentStatus status, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
