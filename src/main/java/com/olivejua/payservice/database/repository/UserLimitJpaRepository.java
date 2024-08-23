package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.UserLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLimitJpaRepository extends JpaRepository<UserLimitEntity, Long> {
}
