package com.finance.loan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.loan.dto.output.LoanRequestOUT;
import com.finance.loan.dto.output.UserDTO;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private int statusCode;
    private String message;

    private LoanRequestOUT loanrequest;
    private List<LoanRequestOUT> loanrequestlist;

    private T data;
}