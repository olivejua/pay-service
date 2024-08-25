package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.PaybackPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaybackPolicyJpaRepository extends JpaRepository<PaybackPolicyEntity, Long> {
}
