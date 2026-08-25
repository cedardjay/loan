package com.finance.loan.dto.input;

import lombok.Data;

@Data
public class IwomiAuthResponse {
    private String message;
    private String status;
    private String token; // will be null if auth failed
}