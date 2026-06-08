package com.finance.loan.dto.output;

import com.finance.loan.entity.LoanStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestOUT {

    // Basic loan information
    private Long requestId;
    private BigDecimal requestedAmount;
    private String description;
    private String purpose;
    private Integer termMonths;
    private BigDecimal interestRate;
    private BigDecimal amountFunded;

    // Status information
    private LoanStatus status;

    // Dates
    private LocalDateTime requestDate;
    private LocalDate deadLine;

    // Borrower information (limited fields, not full User entity)
    private Long borrowerId;
    private String borrowerName;
    private String borrowerEmail;

    // Approver information (if approved/rejected)
    private Long approvedById;
    private String approvedByName;

    // Calculated fields (helpful for clients)
    private BigDecimal remainingAmount;  // requestedAmount - amountFunded
    private Double fundingPercentage;    // (amountFunded / requestedAmount) * 100


}