package com.olivejua.payservice.service.dto;

import java.time.LocalDateTime;

public record AgencyCancelApiResponse(
        LocalDateTime canceledAt
){
}
