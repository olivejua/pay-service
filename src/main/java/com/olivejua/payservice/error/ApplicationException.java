package com.olivejua.payservice.error;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String message;

    public ApplicationException(HttpStatus status, String code, String message) {
        super(message);

        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public FailureResponse toFailureResponse() {
        return new FailureResponse(code, message);
    }
}
