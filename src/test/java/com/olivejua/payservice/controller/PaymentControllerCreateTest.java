package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import com.olivejua.payservice.database.entity.PaymentEntity;
import com.olivejua.payservice.database.repository.PaymentJpaRepository;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
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

//TODO sql 파일 안쓰고, given에 데이터 삽입 다 넣는것도 고민해보기 / 근데 repository가 너무 많이 필요해짐
@Sql(scripts = {"/sql/user-repository-test-data.sql", "/sql/user-limit-repository-test-data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@AutoConfigureMockMvc
@SpringBootTest
class PaymentControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void cleanup() {
        paymentJpaRepository.deleteAll();
    }

    @Test
    void 유저가_활성상태가_아니라면_결제에_실패한다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(2L, 10_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
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
    void 유저의_현재_보유금액과_결제금액_합산금액이_최대한도금액을_초과한다면_결제에_실패한다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 800_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MAX_BALANCE_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Payment amount exceeds the user's maximum allowed balance."));
    }

    //유저의 현재 보유금액 9,000,000 (최대보유금액 10,000,000)
    //이번달 한도금액 7,000,000
    //이번달 결제금액 6,000,000
    @Test
    void 유저의_머니결제금액이_요청월_기준_1달_결제최대한도금액을_초과하면_결제에_실패한다() throws Exception {
        //given
        paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(2_000_000L)
                .transactionId("6400158038980527633")
                .status(PaymentStatus.DONE)
                .createdAt(LocalDateTime.of(2024, 8, 10, 12, 0))
                .approvedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()));

        paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(2_000_000L)
                .transactionId("7992336692739426043")
                .status(PaymentStatus.DONE)
                .createdAt(LocalDateTime.of(2024, 8, 15, 12, 0))
                .approvedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()));

        paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                .user(User.builder().id(1L).build())
                .amount(2_000_000L)
                .transactionId("4972403254390018742")
                .status(PaymentStatus.DONE)
                .createdAt(LocalDateTime.of(2024, 8, 20, 12, 0))
                .approvedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()));

        PaymentCreateRequest request = new PaymentCreateRequest(1L, 1_500_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MONTHLY_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Monthly payment limit exceeded."));
    }

    //유저의 현재 보유금액 9,000,000 (최대보유금액 10,000,000)
    //이번달 한도금액 7,000,000
    //이번달 결제금액 6,000,000
    //1일 한도금액 3,000,000
    //오늘 결제금액 1,500,000
    @Test
    void 유저의_머니결제금액이_요청일자_기준_1일_결제최대한도금액을_초과하면_결제에_실패한다() throws Exception {
        //given
        paymentJpaRepository.save(PaymentEntity.from(Payment.builder()
                        .user(User.builder().id(1L).build())
                        .amount(1_500_000L)
                        .transactionId("aaaaa-aaaaa-aaa")
                        .status(PaymentStatus.DONE)
                        .createdAt(LocalDateTime.now())
                        .approvedAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));

        PaymentCreateRequest request = new PaymentCreateRequest(1L, 2_000_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DAILY_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Daily payment limit exceeded."));
    }

    @Test
    void 유저의_머니결제금액이_1회_결제최대한도금액을_초과하면_결제에_실패한다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 2_100_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SINGLE_PAYMENT_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Single payment limit exceeded."));
    }

    @Disabled
    @Test
    void 결제대행사에서_실패응답이_오면_결제에_실패한다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 10_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_GATEWAY_FAILURE")) //TODO ErrorCode 재정의하기
                .andExpect(jsonPath("$.message").value("Payment processing failed due to external payment gateway error."));
    }

    @Disabled // TODO given에서 sleep 조건 걸기
    @Test
    void 처리시간이_5초_초과되면_결제에_실패한다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 10_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("Payment processing time exceeded the limit of 5 seconds."));
    }

    @Test
    void 모든_조건을_충족하면_결제성공_후_유저_보유금액이_머니_결제금액만큼_업데이트된다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 10_000L);

        //when & then
        mockMvc.perform(post("/pay/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
        ;
    }
}
