package com.finance.loan.utils;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionUtils {

    public static TransactionDTO mapTransactionEntityToOutput(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setSenderName(transaction.getSender().getName());
        dto.setReceiverName(transaction.getReceiver().getName());
        dto.setAmount(transaction.getAmount());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        return dto;
    }

    public static List<TransactionDTO> mapTransactionListToOutput(List<Transaction> transactions) {
        return transactions.stream()
                .map(TransactionUtils::mapTransactionEntityToOutput)
                .collect(Collectors.toList());
    }
}
