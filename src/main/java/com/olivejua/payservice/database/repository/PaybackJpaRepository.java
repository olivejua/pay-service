package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.domain.Payback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaybackJpaRepository extends JpaRepository<Payback, Long> {
}
