package com.bankingsystem.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class BankingException extends RuntimeException {
    private final HttpStatus status;

    public BankingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
