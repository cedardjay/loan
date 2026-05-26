package com.finance.loan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int statusCode;
    private String message;

    private String token;
    private String role;
    private String expirationTime;

    private LoanRequestOUT loanrequest;
    private List<LoanRequestOUT> loanrequestlist;

    private UserDTO user;
    private List<UserDTO> userList;

    private Object data;
}