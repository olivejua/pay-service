package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.UserLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserLimitJpaRepository extends JpaRepository<UserLimitEntity, Long> {
    @Query("select userLimit " +
            "from UserLimitEntity userLimit " +
            "where userLimit.user.id = :userId")
    Optional<UserLimitEntity> findByUserId(@Param("userId") Long userId);
}
