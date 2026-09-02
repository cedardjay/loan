package com.finance.loan.utils;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import com.finance.loan.entity.ScheduleStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanCalculatorUtils {

    public static BigDecimal calculateExpectedReturn(BigDecimal investedAmount,
                                                     BigDecimal interestRate,
                                                     int termMonths) {
        return investedAmount
                .multiply(interestRate
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(termMonths)
                        .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    //mock function to calculate repayment schedules
    public static List<RepaymentSchedule> buildSchedule(LoanRequest loanRequest) {
        int termMonths = loanRequest.getTermMonths();
        BigDecimal principal = loanRequest.getRequestedAmount();
        BigDecimal annualRate = loanRequest.getInterestRate();

        BigDecimal totalInterest = principal
                .multiply(annualRate)
                .multiply(BigDecimal.valueOf(termMonths))
                .divide(BigDecimal.valueOf(100 * 12), 0, RoundingMode.HALF_UP);

        BigDecimal totalRepayable = principal.add(totalInterest);

        // Base monthly figures, rounded to whole units
        BigDecimal monthlyInstallment = totalRepayable
                .divide(BigDecimal.valueOf(termMonths), 0, RoundingMode.HALF_UP);
        BigDecimal monthlyInterest = totalInterest
                .divide(BigDecimal.valueOf(termMonths), 0, RoundingMode.HALF_UP);
        BigDecimal monthlyPrincipal = principal
                .divide(BigDecimal.valueOf(termMonths), 0, RoundingMode.HALF_UP);

        List<RepaymentSchedule> schedules = new ArrayList<>();
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        BigDecimal runningTotalDue = BigDecimal.ZERO;
        BigDecimal runningPrincipal = BigDecimal.ZERO;
        BigDecimal runningInterest = BigDecimal.ZERO;

        for (int i = 1; i <= termMonths; i++) {
            boolean isLast = (i == termMonths);

            BigDecimal amountDue;
            BigDecimal principalComponent;
            BigDecimal interestComponent;

            if (isLast) {
                // Absorber: last installment takes whatever is left,
                // so sum of all installments == totalRepayable exactly.
                amountDue = totalRepayable.subtract(runningTotalDue);
                principalComponent = principal.subtract(runningPrincipal);
                interestComponent = totalInterest.subtract(runningInterest);
            } else {
                amountDue = monthlyInstallment;
                principalComponent = monthlyPrincipal;
                interestComponent = monthlyInterest;

                runningTotalDue = runningTotalDue.add(amountDue);
                runningPrincipal = runningPrincipal.add(principalComponent);
                runningInterest = runningInterest.add(interestComponent);
            }

            schedules.add(RepaymentSchedule.builder()
                    .loanRequest(loanRequest)
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .amountDue(amountDue)
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
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