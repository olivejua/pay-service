package com.olivejua.payservice.database.repository;

import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.domain.type.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    // TODO 오늘, 이번달 거래한 결제금액 계산을 DB연산으로 할지 다 가져와서 연산할지 고민하기 -> 당연히 목록조회해서 비즈니스 로직에서 계산
    // TODO 비교할 컬럼을 생성일자로 해야할까? -> 사실 이건 어느걸로 해도 임의로 하는거니 수정할 수 있는 부분이다.
    // TODO 날짜 계산도 빅데이터가 넘어가면 날짜에 컬럼을 추가해서 하는 것을 고려한다.
    @Query("SELECT COALESCE(SUM(payment.amount), 0) " +
            "FROM PaymentEntity payment " +
            "WHERE payment.user.id = :userId " +
            "AND payment.status = :status " +
            "AND payment.createdAt >= :startOfDay " +
            "AND payment.createdAt <= :endOfDay")
    long findTotalAmountByUserIdAndStatusAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    List<PaymentEntity> findAllByUserIdAndStatusAndCreatedAtBetween(Long userId, PaymentStatus status, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
