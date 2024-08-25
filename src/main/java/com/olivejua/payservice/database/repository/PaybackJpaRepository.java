package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.PaybackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaybackJpaRepository extends JpaRepository<PaybackEntity, Long> {
    boolean existsByPaymentId(Long paymentId);

    Optional<PaybackEntity> findByPaymentId(Long paymentId);
}
