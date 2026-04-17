package com.finance.loan.entity;

import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.*;
import lombok.Data;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_requests")
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private BigDecimal amountFunded = BigDecimal.ZERO; //setting initial amount to zero

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanState state = LoanState.PENDING_APPROVAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.NOT_FUNDED;

    @Column(updatable = false)
    private LocalDateTime requestDate;

    private LocalDate deadLine; //admin sets Expiry date for the loanRequest

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id")
    private User approval; //Approved or rejected

    @OneToMany(mappedBy = "loanRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchedRequest> matchedRequests = new ArrayList<>();
}