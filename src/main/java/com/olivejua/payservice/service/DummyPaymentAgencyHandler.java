package com.olivejua.payservice.service;

import com.olivejua.payservice.domain.Payment;
import com.olivejua.payservice.service.dto.AgencyCancelApiResponse;
import com.olivejua.payservice.service.dto.AgencyPayApiResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DummyPaymentAgencyHandler {
    private final RestTemplate restTemplate;

    public DummyPaymentAgencyHandler() {
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 결제 요청후 응답이 5초이내 오지 않으면 timeout 예외를 던진다.
     * TODO Response 별도 생성할지는 고려해보기
     */
    public AgencyPayApiResponse requestPaymentFromAgency(Payment payment) {
        String transactionId = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        LocalDateTime approvedAt = LocalDateTime.now();

        return new AgencyPayApiResponse(transactionId, approvedAt);
    }

    public AgencyCancelApiResponse requestCancellationFromAgency(Payment payment) {
        LocalDateTime canceledAt = LocalDateTime.now();

        return new AgencyCancelApiResponse(canceledAt);
    }
}
