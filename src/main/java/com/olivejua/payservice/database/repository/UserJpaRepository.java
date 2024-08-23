package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
}
