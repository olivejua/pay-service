package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olivejua.payservice.controller.request.PaybackCreateRequest;
import com.olivejua.payservice.database.entity.PaybackEntity;
import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.database.repository.PaybackJpaRepository;
import com.olivejua.payservice.database.repository.PaymentJpaRepository;
import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.PaybackPolicy;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.type.PaybackStatus;
import com.olivejua.payservice.domain.type.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"/sql/user-repository-test-data.sql", "/sql/user-limit-repository-test-data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@AutoConfigureMockMvc
@SpringBootTest
class PaybackControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;
    
    @Autowired
    private PaybackJpaRepository paybackJpaRepository;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void cleanup() {
        paybackJpaRepository.deleteAll();
        paymentJpaRepository.deleteAll();
    }

    @Test
    void 유저가_활성상태가_아니라면_결제에_실패한다() throws Exception {
        //given
        PaybackCreateRequest request = new PaybackCreateRequest(2L, 1L);

        //when & then
        mockMvc.perform(post("/pay/paybacks")
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
    void 결제정보가_존재하지_않으면_실패한다() throws Exception {
        //given
        PaybackCreateRequest request = new PaybackCreateRequest(1L, 1L);

        //when & then
        mockMvc.perform(post("/pay/paybacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment information not found."));
    }

    @Sql({"/sql/payback-policy-repository-test-data.sql"})
    @Test
    void 결제건의_페이백이_존재한다면_실패한다() throws Exception {
        //given
        LocalDateTime paymentCreatedDateTime = LocalDateTime.of(2024, 8, 1, 12, 0);
        Payment savedPayment = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.COMPLETED)
                .createdAt(paymentCreatedDateTime)
                .approvedAt(paymentCreatedDateTime)
                .updatedAt(paymentCreatedDateTime)
                .build())).toModel();

        paybackJpaRepository.save(PaybackEntity.from(Payback.builder()
                        .policy(PaybackPolicy.builder().id(1L).build())
                        .payment(savedPayment)
                        .amount(1_000L)
                        .status(PaybackStatus.COMPLETED)
                        .createdAt(paymentCreatedDateTime)
                        .updatedAt(paymentCreatedDateTime)
                .build()));

        PaybackCreateRequest request = new PaybackCreateRequest(1L, savedPayment.getId());

        //when & then
        mockMvc.perform(post("/pay/paybacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYBACK_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Payback for this payment already exists."));
    }

    @Test
    void 현재_적용가능한_정책이_존재하지_않는다면_페이백을_처리하지_않고_종료한다() throws Exception {
        //given
        LocalDateTime paymentCreatedDateTime = LocalDateTime.of(2024, 8, 1, 12, 0);
        Long savedPaymentId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.COMPLETED)
                .createdAt(paymentCreatedDateTime)
                .approvedAt(paymentCreatedDateTime)
                .updatedAt(paymentCreatedDateTime)
                .build())).getId();

        PaybackCreateRequest request = new PaybackCreateRequest(1L, savedPaymentId);

        //when & then
        mockMvc.perform(post("/pay/paybacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Disabled // TODO given에서 sleep 조건 걸기
    @Sql({"/sql/payback-policy-repository-test-data.sql"})
    @Test
    void 처리시간이_5초_초과되면_결제에_실패한다() throws Exception {
        //given
        LocalDateTime paymentCreatedDateTime = LocalDateTime.of(2024, 8, 1, 12, 0);
        Long savedPaymentId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.COMPLETED)
                .createdAt(paymentCreatedDateTime)
                .approvedAt(paymentCreatedDateTime)
                .updatedAt(paymentCreatedDateTime)
                .build())).getId();

        PaybackCreateRequest request = new PaybackCreateRequest(1L, savedPaymentId);

        //when & then
        mockMvc.perform(post("/pay/paybacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("Payment processing time exceeded the limit of 5 seconds."));
    }

    @Sql({"/sql/payback-policy-repository-test-data.sql"})
    @Test
    void 모든_조건을_충족하면_페이백상태를_변경하고_유저의_보유금액에서_페이백금액이_업데이트된다() throws Exception {
        //given
        LocalDateTime paymentCreatedDateTime = LocalDateTime.of(2024, 8, 1, 12, 0);
        Long savedPaymentId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.COMPLETED)
                .createdAt(paymentCreatedDateTime)
                .approvedAt(paymentCreatedDateTime)
                .updatedAt(paymentCreatedDateTime)
                .build())).getId();

        PaybackCreateRequest request = new PaybackCreateRequest(1L, savedPaymentId);

        //when & then
        mockMvc.perform(post("/pay/paybacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
        //TODO 유저 잔액 검증하기
    }
}
