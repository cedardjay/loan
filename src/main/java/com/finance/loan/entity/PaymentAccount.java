package com.finance.loan.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "payment_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account_number"}))
public class PaymentAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Boolean isDefault = true;

}