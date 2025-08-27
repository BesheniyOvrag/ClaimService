package com.example.claim_checker.exception;



import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Getter
@Setter
public class InvalidUserClaimException extends RuntimeException {
    private final HttpStatus httpStatus;

    public InvalidUserClaimException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}