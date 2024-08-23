package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.PaybackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaybackJpaRepository extends JpaRepository<PaybackEntity, Long> {
}
