package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olivejua.payservice.controller.request.PaymentCancelRequest;
import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.database.repository.PaymentJpaRepository;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.type.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlGroup({
        @Sql(scripts = {"/sql/user-repository-test-data.sql", "/sql/user-limit-repository-test-data.sql"},
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS),
        @Sql(scripts = {"/sql/delete-all-test-data.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
})
@AutoConfigureMockMvc
@SpringBootTest
class PaymentControllerCancelTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @AfterEach
    void cleanup() {
        paymentJpaRepository.deleteAll();
    }

    @Test
    void 유저가_활성상태가_아니라면_결제취소에_실패한다() throws Exception {
        //given
        Long userId = 2L;
        PaymentCancelRequest request = new PaymentCancelRequest(userId);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND_OR_WITHDRAWN"))
                .andExpect(jsonPath("$.message").value("User does not exist or is in a withdrawn state."));
    }

    @Test
    void 결제정보가_존재하지_않으면_결제취소에_실패한다() throws Exception {
        //given
        Long userId = 1L;
        PaymentCancelRequest request = new PaymentCancelRequest(userId);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment information not found."));
    }

    @Test
    void 유저가_결제자가_아닌_경우_결제취소에_실패한다() throws Exception {
        //given
        Long payerId = 1L;
        Long requesterId = 3L;

        Long savedId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(payerId).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.of(2024, 8, 1, 12, 0))
                .approvedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build())).getId();

        PaymentCancelRequest request = new PaymentCancelRequest(requesterId);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", savedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_CANCELLATION"))
                .andExpect(jsonPath("$.message").value("The requester is not authorized to cancel this payment."));
    }

    @Test
    void 요청결제취소건의_상태가_이미_취소일_경우_결제에_실패한다() throws Exception {
        //given
        Long savedId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.CANCELLED)
                .createdAt(LocalDateTime.of(2024, 8, 1, 12, 0))
                .approvedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .canceledAt(LocalDateTime.now())
                .build())).getId();

        Long userId = 1L;
        PaymentCancelRequest request = new PaymentCancelRequest(userId);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", savedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_CANCELED"))
                .andExpect(jsonPath("$.message").value("The payment has already been canceled."));
    }

    @Test
    void 모든_조건을_충족하면_결제성공_후_유저_보유금액이_머니_결제금액만큼_업데이트된다() throws Exception {
        //given
        Long savedId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.of(2024, 8, 1, 12, 0))
                .approvedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build())).getId();

        Long userId = 1L;
        PaymentCancelRequest request = new PaymentCancelRequest(userId);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", savedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(PaymentStatus.CANCEL_PENDING.toString()))
                .andExpect(jsonPath("$.canceledAt").doesNotExist())
        ;
    }
}
