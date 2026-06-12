package com.finance.loan.exception;

import lombok.Getter;

@Getter
public class OurException extends RuntimeException {
    private final int status;

    public OurException(String message, int status) {
        super(message);
        this.status = status;
    }
}