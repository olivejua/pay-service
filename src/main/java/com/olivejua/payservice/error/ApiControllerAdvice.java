package com.olivejua.payservice.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<FailureResponse> handleApplicationException(ApplicationException exception) {

        return ResponseEntity
                .status(exception.getStatus())
                .body(exception.toFailureResponse());
    }
}
