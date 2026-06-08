package com.finance.loan.dto.output;

import com.finance.loan.entity.ScheduleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RepaymentScheduleDTO {
    private Long scheduleId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal amountPaid;
    private ScheduleStatus status;
    private LocalDate paidDate;
}
