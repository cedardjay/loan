package com.finance.loan.utils;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionUtils {

    public static TransactionDTO mapTransactionEntityToOutput(Transaction transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .senderName(transaction.getSender().getName())
                .receiverName(transaction.getReceiver().getName())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentReference(transaction.getPaymentReference())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .build();
    }

    public static List<TransactionDTO> mapTransactionListToOutput(List<Transaction> transactions) {
        return transactions.stream()
                .map(TransactionUtils::mapTransactionEntityToOutput)
                .collect(Collectors.toList());
    }
}
