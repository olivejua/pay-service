package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.PaybackPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaybackPolicyJpaRepository extends JpaRepository<PaybackPolicyEntity, Long> {
    List<PaybackPolicyEntity> findAllByIsActive(Boolean isActive);
}
