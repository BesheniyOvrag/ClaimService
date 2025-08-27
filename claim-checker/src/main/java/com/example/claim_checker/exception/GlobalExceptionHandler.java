package com.example.claim_checker.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUserClaimException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserClaim(
            InvalidUserClaimException ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");

        ErrorResponse response = new ErrorResponse(
                ex.getHttpStatus(),
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }
}