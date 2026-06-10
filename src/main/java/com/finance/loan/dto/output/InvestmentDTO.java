package com.finance.loan.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvestmentDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private BigDecimal interest;
    private String status;
    private String investedDate;
    private BigDecimal expectedReturn;
    private String investorEmail;
}