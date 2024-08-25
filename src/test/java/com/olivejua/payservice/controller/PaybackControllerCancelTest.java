package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olivejua.payservice.controller.request.PaybackCancelRequest;
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

@Sql(scripts = {"/sql/user-repository-test-data.sql", "/sql/user-limit-repository-test-data.sql", "/sql/payback-policy-repository-test-data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@AutoConfigureMockMvc
@SpringBootTest
class PaybackControllerCancelTest {

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
    void 유저가_활성상태가_아니라면_페이백취소에_실패한다() throws Exception {
        //given
        PaybackCancelRequest request = new PaybackCancelRequest(2L, 1L);

        //when & then
        mockMvc.perform(post("/pay/paybacks/cancel")
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
    void 결제_취소건의_페이백이_존재하지_않다면_페이백취소_처리하지않고_종료한다() throws Exception {
        //given
        PaybackCancelRequest request = new PaybackCancelRequest(1L, 1L);

        //when & then
        mockMvc.perform(post("/pay/paybacks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Sql("/sql/payment-repository-test-data.sql")
    @Test
    void 결제_취소건의_페이백이_이미_취소상태인_경우_페이백취소에_실패한다() throws Exception {
        //given
        Long givenPaymentId = 1L;
        paybackJpaRepository.save(PaybackEntity.from(Payback.builder()
                        .policy(PaybackPolicy.builder().id(1L).build())
                        .payment(Payment.builder()
                                .id(givenPaymentId)
                                .user(User.builder()
                                        .id(1L)
                                        .build())
                                .build())
                        .status(PaybackStatus.CANCELED)
                        .amount(1000L)
                        .createdAt(LocalDateTime.of(2024, 8, 20, 12, 0, 0))
                        .updatedAt(LocalDateTime.of(2024, 8, 21, 12, 0, 0))
                        .canceledAt(LocalDateTime.of(2024, 8, 21, 12, 0, 0))
                .build()));

        PaybackCancelRequest request = new PaybackCancelRequest(1L, givenPaymentId);

        //when & then
        mockMvc.perform(post("/pay/paybacks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_CANCELED"))
                .andExpect(jsonPath("$.message").value("The payback has already been canceled."));
    }

    @Disabled // TODO given에서 sleep 조건 걸기
    @Sql({"/sql/payback-policy-repository-test-data.sql"})
    @Test
    void 처리시간이_5초_초과되면_페이백취소에_실패한다() throws Exception {
        //given
        LocalDateTime paymentCreatedDateTime = LocalDateTime.of(2024, 8, 1, 12, 0);
        Long savedPaymentId = paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(1_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.DONE)
                .createdAt(paymentCreatedDateTime)
                .approvedAt(paymentCreatedDateTime)
                .updatedAt(paymentCreatedDateTime)
                .build())).getId();

        PaybackCreateRequest request = new PaybackCreateRequest(1L, savedPaymentId);

        //when & then
        mockMvc.perform(post("/pay/paybacks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("Payment processing time exceeded the limit of 5 seconds."));
    }

    @Sql("/sql/payment-repository-test-data.sql")
    @Test
    void 모든_조건을_충족하면_페이백상태를_변경하고_유저의_보유금액에서_페이백금액이_차감된다() throws Exception {
        //given
        Long givenPaymentId = 1L;
        Payback givenPayback = Payback.builder()
                .policy(PaybackPolicy.builder().id(1L).build())
                .payment(Payment.builder()
                        .id(givenPaymentId)
                        .user(User.builder()
                                .id(1L)
                                .build())
                        .build())
                .status(PaybackStatus.COMPLETED)
                .amount(1000L)
                .createdAt(LocalDateTime.of(2024, 8, 20, 12, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 8, 20, 12, 0, 0))
                .build();
        paybackJpaRepository.save(PaybackEntity.from(givenPayback));

        PaybackCancelRequest request = new PaybackCancelRequest(1L, givenPaymentId);

        //when & then
        mockMvc.perform(post("/pay/paybacks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.paymentId").value(givenPaymentId))
                .andExpect(jsonPath("$.amount").value(givenPayback.getAmount()))
                .andExpect(jsonPath("$.status").value(PaybackStatus.CANCELED.toString()))
//                .andExpect(jsonPath("$.createdAt").value(givenPayback.getCreatedAt()))
//                .andExpect(jsonPath("$.updatedAt").value(givenPayback.getUpdatedAt()))
                .andExpect(jsonPath("$.canceledAt").exists()); // TODO 시간 검증 테스트 주입 추가할지 고민해보기

        //TODO 유저 잔액 검증하기
    }
}
