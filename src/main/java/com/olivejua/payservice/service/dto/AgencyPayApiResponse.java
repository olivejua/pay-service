package com.olivejua.payservice.service.dto;

import java.time.LocalDateTime;

public record AgencyPayApiResponse(
        String transactionId,
        LocalDateTime approvedAt
){
}
