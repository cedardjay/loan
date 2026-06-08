package com.finance.loan.utils;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import com.finance.loan.entity.ScheduleStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanCalculatorUtil {

    //mock function to calculate repayment schedules
    public static List<RepaymentSchedule> buildSchedule(LoanRequest loanRequest) {
        int termMonths = loanRequest.getTermMonths();
        BigDecimal principal = loanRequest.getRequestedAmount();
        BigDecimal annualRate = loanRequest.getInterestRate();

        BigDecimal totalInterest = principal
                .multiply(annualRate)
                .multiply(BigDecimal.valueOf(termMonths))
                .divide(BigDecimal.valueOf(100 * 12), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyInstallment = principal.add(totalInterest)
                .divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyInterest = totalInterest
                .divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyPrincipal = principal
                .divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

        List<RepaymentSchedule> schedules = new ArrayList<>();
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= termMonths; i++) {
            schedules.add(RepaymentSchedule.builder()
                    .loanRequest(loanRequest)
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .amountDue(monthlyInstallment)
                    .principalComponent(monthlyPrincipal)
                    .interestComponent(monthlyInterest)
                    .amountPaid(BigDecimal.ZERO)
                    .status(ScheduleStatus.PENDING)
                    .build());

            dueDate = dueDate.plusMonths(1);
        }

        return schedules;
    }

    //mock function to calculate interest rate of loan request
    public static BigDecimal calculateInterestRate(BigDecimal amount, Integer termMonths) {
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return new BigDecimal("5.5");
        } else if (termMonths > 24) {
            return new BigDecimal("6.0");
        } else {
            return new BigDecimal("7.0");
        }
    }
}