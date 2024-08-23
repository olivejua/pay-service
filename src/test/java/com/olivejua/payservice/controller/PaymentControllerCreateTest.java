package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olivejua.payservice.controller.request.PaymentCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class PaymentControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper = objectMapper;
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
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 600_000L);

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

    @Test
    void 유저의_머니결제금액이_요청월_기준_1달_결제최대한도금액을_초과하면_결제에_실패한다() throws Exception {
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
                .andExpect(jsonPath("$.code").value("MONTHLY_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Monthly payment limit exceeded."));
    }

    @Test
    void 유저의_머니결제금액이_요청일자_기준_1일_결제최대한도금액을_초과하면_결제에_실패한다() throws Exception {
        //given
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 1_000_000L);

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
        PaymentCreateRequest request = new PaymentCreateRequest(1L, 2_000_000L);

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
