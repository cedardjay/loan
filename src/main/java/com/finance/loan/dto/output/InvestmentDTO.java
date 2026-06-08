package com.finance.loan.dto.output;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InvestmentDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private BigDecimal interest;
    private String status;
    private String investedDate;
    private BigDecimal expectedReturn;
}