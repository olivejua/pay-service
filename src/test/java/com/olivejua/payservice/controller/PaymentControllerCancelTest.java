package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.olivejua.payservice.controller.request.PaymentCancelRequest;
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
class PaymentControllerCancelTest {
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
    void 유저가_활성상태가_아니라면_결제취소에_실패한다() throws Exception {
        //given
        PaymentCancelRequest request = new PaymentCancelRequest(2L);

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
        PaymentCancelRequest request = new PaymentCancelRequest(1L);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 2L)
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
        PaymentCancelRequest request = new PaymentCancelRequest(3L);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 2L)
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
        PaymentCancelRequest request = new PaymentCancelRequest(1L);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_CANCELED"))
                .andExpect(jsonPath("$.message").value("The payment has already been canceled."));
    }

    @Disabled
    @Test
    void 결제대행사에서_실패응답이_오면_결제에_실패한다() throws Exception {
        //given
        PaymentCancelRequest request = new PaymentCancelRequest(1L);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 1L)
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
        PaymentCancelRequest request = new PaymentCancelRequest(1L);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 1L)
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
        PaymentCancelRequest request = new PaymentCancelRequest(1L);

        //when & then
        mockMvc.perform(post("/pay/payments/{paymentId}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
