package com.finance.loan.service.implementation;


import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.RepaymentScheduleRepository;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.utils.LoanCalculatorUtils;
import com.finance.loan.utils.RepaymentScheduleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class RepaymentScheduleService implements IRepaymentScheduleService {

    @Autowired
    private RepaymentScheduleRepository repaymentScheduleRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;


    public void generateSchedule(LoanRequest loanRequest) {
        repaymentScheduleRepository.saveAll(LoanCalculatorUtils.buildSchedule(loanRequest));
    }


    public List<RepaymentScheduleDTO> getRepaymentScheduleByLoanRequest(Long loanRequestId) {
        // --- FETCH ---
        if (!loanRequestRepository.existsById(loanRequestId)) {
            throw new OurException("Loan request not found", 404);
        }

        // --- RETURN ---
        return RepaymentScheduleUtils.mapRepaymentScheduleListToOutput(
                repaymentScheduleRepository.findByLoanRequest_requestId(loanRequestId));
    }


    public List<RepaymentScheduleDTO> getMyRepaymentSchedule(Long loanRequestId, String email) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId, 404));

        // --- VALIDATE ---
        if (!loanRequest.getBorrower().getEmail().equals(email)) {
            throw new OurException("You are not authorized", 403);
        }

        // --- RETURN ---
        return RepaymentScheduleUtils.mapRepaymentScheduleListToOutput(
                repaymentScheduleRepository.findByLoanRequest_requestId(loanRequestId));
    }
}