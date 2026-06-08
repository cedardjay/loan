package com.finance.loan.dto.output;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioSummaryDTO {
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal totalReturns;
    private BigDecimal avgApy;
}
