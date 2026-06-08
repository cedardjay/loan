package com.finance.loan.utils;

import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.dto.output.UserDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static UserDTO mapUserEntityToDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole().name());

        return userDTO;
    }

    public static List<UserDTO> mapUserListToDTO(List<User> userList) {
        return userList.stream()
                .map(Utils::mapUserEntityToDTO)
                .collect(Collectors.toList());
    }


    // method to create loan request out from entity
    public static LoanRequestDTO mapLoanRequestEntityToDTO(LoanRequest loanRequest) {
        LoanRequestDTO output = new LoanRequestDTO();

        // Basic fields
        output.setRequestId(loanRequest.getRequestId());
        output.setRequestedAmount(loanRequest.getRequestedAmount());
        output.setDescription(loanRequest.getDescription());
        output.setPurpose(loanRequest.getPurpose());
        output.setTermMonths(loanRequest.getTermMonths());
        output.setInterestRate(loanRequest.getInterestRate());
        output.setAmountFunded(loanRequest.getAmountFunded());
        output.setStatus(loanRequest.getStatus());
        output.setRequestDate(loanRequest.getRequestDate());
        output.setDeadLine(loanRequest.getDeadLine());

        // Borrower info (handle lazy loading safely)
        if (loanRequest.getBorrower() != null) {
            output.setBorrowerId(loanRequest.getBorrower().getId());
            output.setBorrowerName(loanRequest.getBorrower().getName());
            output.setBorrowerEmail(loanRequest.getBorrower().getEmail());
        }

        // Approver info
        if (loanRequest.getApproval() != null) {
            output.setApprovedById(loanRequest.getApproval().getId());
            output.setApprovedByName(loanRequest.getApproval().getName());
        }

        // Calculated fields
        if (loanRequest.getRequestedAmount() != null &&
                loanRequest.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0) {

            output.setRemainingAmount(loanRequest.getRequestedAmount()
                    .subtract(loanRequest.getAmountFunded()));

            double percentage = loanRequest.getAmountFunded()
                    .divide(loanRequest.getRequestedAmount(), 4, RoundingMode.HALF_UP)
                    .doubleValue() * 100;
            output.setFundingPercentage(percentage);
        } else {
            output.setRemainingAmount(BigDecimal.ZERO);
            output.setFundingPercentage(0.0);
        }

        return output;
    }

    public static List<LoanRequestDTO> mapLoanRequestListToDTO(List<LoanRequest> loanRequestList) {
        return loanRequestList.stream()
                .map(Utils::mapLoanRequestEntityToDTO)
                .collect(Collectors.toList());
    }

}