package com.finance.loan.service.implementation;

import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.RepaymentScheduleRepository;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.utils.LoanCalculatorUtil;
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
        List<RepaymentSchedule> schedules = LoanCalculatorUtil.buildSchedule(loanRequest);
        repaymentScheduleRepository.saveAll(schedules);
    }


    //GET ALL REPAYMENT SCHEDULE BY LOAN REQUEST
    public Response<List<RepaymentScheduleDTO>> getRepaymentScheduleByLoanRequest(long loanRequestId) {
        Response<List<RepaymentScheduleDTO>> response = new Response<>();
        try {

            // --- FETCH ---
            if (!loanRequestRepository.existsById(loanRequestId)) {
                throw new OurException("Loan request not found");
            }
            List<RepaymentSchedule> schedules = repaymentScheduleRepository
                    .findByLoanRequest_requestId(loanRequestId);

            // --- VALIDATE ---
            if (schedules.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("No repayment schedule found for loan request: " + loanRequestId);
                return response;
            }

            // --- EXECUTE ---
            // no computation needed, data is already in the right shape

            // --- PERSIST ---
            // read-only operation, nothing to save

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Repayment schedule retrieved successfully");
            response.setData(RepaymentScheduleUtils.mapRepaymentScheduleListToDTO(schedules));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving repayment schedule: " + e.getMessage());
        }
        return response;
    }
}
