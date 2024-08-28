package com.olivejua.payservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olivejua.payservice.controller.request.PaybackCancelRequest;
import com.olivejua.payservice.database.entity.PaybackEntity;
import com.olivejua.payservice.database.repository.PaybackJpaRepository;
import com.olivejua.payservice.domain.Payback;
import com.olivejua.payservice.domain.PaybackPolicy;
import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.domain.User;
import com.olivejua.payservice.domain.type.PaybackStatus;
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
        @Sql(scripts = {"/sql/user-repository-test-data.sql", "/sql/user-limit-repository-test-data.sql", "/sql/payback-policy-repository-test-data.sql"},
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS),
        @Sql(scripts = {"/sql/delete-all-test-data.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
})
@AutoConfigureMockMvc
@SpringBootTest
class PaybackControllerCancelTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaybackJpaRepository paybackJpaRepository;

    @AfterEach
    void cleanup() {
        paybackJpaRepository.deleteAll();
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
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.canceledAt").exists());
    }
}
