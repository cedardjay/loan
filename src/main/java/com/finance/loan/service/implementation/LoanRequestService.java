package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.dto.output.PaymentResult;
import com.finance.loan.entity.*;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.dto.Response;
import com.finance.loan.service.interfac.ILoanRequestService;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class LoanRequestService implements ILoanRequestService {

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private UserRepository userRepository;

@Autowired
private ITransactionService transactionService;

@Autowired
private IRepaymentScheduleService repaymentScheduleService;

@Autowired
private IPaymentGatewayService paymentGatewayService;

    // CREATE A LOAN REQUEST
    public Response<LoanRequestDTO> createLoanRequest(LoanRequestIN requestDTO, String email) {
        Response<LoanRequestDTO> response = new Response<>();
        try {
            // --- FETCH ---
            User borrower = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            // --- EXECUTE ---
            BigDecimal interestRate = LoanCalculatorUtils.calculateInterestRate(
                    requestDTO.getRequestedAmount(),
                    requestDTO.getTermMonths()
            );

            // --- PERSIST ---
            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setBorrower(borrower);
            loanRequest.setRequestedAmount(requestDTO.getRequestedAmount());
            loanRequest.setDescription(requestDTO.getDescription());
            loanRequest.setTermMonths(requestDTO.getTermMonths());
            loanRequest.setPurpose(requestDTO.getPurpose());
            loanRequest.setAmountFunded(BigDecimal.ZERO);
            loanRequest.setStatus(LoanStatus.PENDING_APPROVAL);
            loanRequest.setRequestDate(LocalDateTime.now());
            loanRequest.setInterestRate(interestRate);

            LoanRequest savedRequest = loanRequestRepository.save(loanRequest);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan request created successfully");
            response.setData(Utils.mapLoanRequestEntityToDTO(savedRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error creating loan request: " + e.getMessage());
        }
        return response;
    }

    // GET MY REQUESTS
    public Response<List<LoanRequestDTO>> getLoanRequestsByBorrowerEmail(String email) {
        Response<List<LoanRequestDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            List<LoanRequest> loanRequests = loanRequestRepository.findByBorrowerEmail(email);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("User's loan requests fetched successfully");
            response.setData(Utils.mapLoanRequestListToDTO(loanRequests));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching user's loan requests: " + e.getMessage());
        }
        return response;
    }


    // GET MY REQUEST BY REQUEST ID
    public Response<LoanRequestDTO> getMyLoanRequestById(Long requestId, String email) {
        Response<LoanRequestDTO> response = new Response<>();
        try {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            // --- VALIDATE ---
            if (!loanRequest.getBorrower().getEmail().equals(email)) {
                response.setStatusCode(403);
                response.setMessage("You are not authorized to view this loan request");
                return response;
            }

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan request fetched successfully");
            response.setData(Utils.mapLoanRequestEntityToDTO(loanRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan request: " + e.getMessage());
        }
        return response;
    }


    // DELETE
    public Response<Void> deleteLoanRequest(Long requestId, String email) {
        Response<Void> response = new Response<>();
        try {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            // --- VALIDATE ---
            if (!loanRequest.getBorrower().getEmail().equals(email)) {
                response.setStatusCode(403);
                response.setMessage("You are not authorized to delete this loan request");
                return response;
            }

            if (loanRequest.getStatus() == LoanStatus.FULLY_FUNDED ||
                    loanRequest.getAmountFunded().compareTo(BigDecimal.ZERO) > 0) {
                response.setStatusCode(400);
                response.setMessage("Cannot delete a loan request that has been funded");
                return response;
            }

            // --- PERSIST ---
            loanRequestRepository.delete(loanRequest);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan request deleted successfully");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting loan request: " + e.getMessage());
        }
        return response;
    }


    // GET ALL LOAN REQUESTS (ADMIN and SUPERADMIN)
    public Response<List<LoanRequestDTO>> getAllLoanRequests() {
        Response<List<LoanRequestDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            List<LoanRequest> loanRequests = loanRequestRepository.findAll();

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan requests fetched successfully");
            response.setData(Utils.mapLoanRequestListToDTO(loanRequests));

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan requests: " + e.getMessage());
        }
        return response;
    }


    // GET REQUEST BY ID OF REQUEST (ADMIN and SUPERADMIN)
    public Response<LoanRequestDTO> getLoanRequestById(Long requestId) {
        Response<LoanRequestDTO> response = new Response<>();
        try {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan request fetched successfully");
            response.setData(Utils.mapLoanRequestEntityToDTO(loanRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan request: " + e.getMessage());
        }
        return response;
    }


    // GET LOAN REQUESTS BY BORROWER ID (ADMIN and SUPERADMIN)
    public Response<List<LoanRequestDTO>> getLoanRequestsByBorrowerId(Long borrowerId) {
        Response<List<LoanRequestDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            userRepository.findById(borrowerId)
                    .orElseThrow(() -> new OurException("User not found with id: " + borrowerId));

            List<LoanRequest> loanRequests = loanRequestRepository.findByBorrower_Id(borrowerId);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan requests fetched successfully");
            response.setData(Utils.mapLoanRequestListToDTO(loanRequests));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan requests: " + e.getMessage());
        }
        return response;
    }


    // APPROVE LOAN REQUEST (ADMIN / SUPERADMIN)
    public Response<LoanRequestDTO> approveLoanRequest(Long requestId) {
        Response<LoanRequestDTO> response = new Response<>();
        try {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            // --- VALIDATE ---
            if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
                response.setStatusCode(400);
                response.setMessage("Only pending loan requests can be approved");
                return response;
            }

            // --- PERSIST ---
            loanRequest.setStatus(LoanStatus.APPROVED);
            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan request approved successfully");
            response.setData(Utils.mapLoanRequestEntityToDTO(updatedRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error approving loan request: " + e.getMessage());
        }
        return response;
    }


    // REJECT LOAN REQUEST (ADMIN / SUPERADMIN)
    public Response<LoanRequestDTO> rejectLoanRequest(Long requestId) {
        Response<LoanRequestDTO> response = new Response<>();
        try {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            // --- VALIDATE ---
            if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
                response.setStatusCode(400);
                response.setMessage("Only pending loan requests can be rejected");
                return response;
            }

            // --- PERSIST ---
            loanRequest.setStatus(LoanStatus.REJECTED);
            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan request rejected successfully");
            response.setData(Utils.mapLoanRequestEntityToDTO(updatedRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error rejecting loan request: " + e.getMessage());
        }
        return response;
    }


    // GET MARKETPLACE LOANS
    public Response<List<LoanRequestDTO>> getMarketplaceLoans() {
        Response<List<LoanRequestDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            List<LoanRequest> loanRequests = loanRequestRepository.findByStatusIn(List.of(
                    LoanStatus.APPROVED,
                    LoanStatus.PARTIALLY_FUNDED,
                    LoanStatus.FULLY_FUNDED
            ));

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Marketplace loans fetched successfully");
            response.setData(Utils.mapLoanRequestListToDTO(loanRequests));

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching marketplace loans: " + e.getMessage());
        }
        return response;
    }


    // DISBURSE LOAN
    @Transactional
    public Response<LoanRequestDTO> disburseLoan(Long requestId, String adminEmail) {
        Response<LoanRequestDTO> response = new Response<>();
        try {
            // --- FETCH ---
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new OurException("Admin user not found"));

            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            // --- VALIDATE ---///////to come back add other constraints
            if (loanRequest.getStatus() != LoanStatus.FULLY_FUNDED) {
                response.setStatusCode(400);
                response.setMessage("Only fully funded loan requests can be disbursed");
                return response;
            }

            if (loanRequest.getBorrower() == null) {
                response.setStatusCode(400);
                response.setMessage("No borrower linked to this loan request");
                return response;
            }

            // --- EXECUTE ---
            String paymentReference = "DISB-" + UUID.randomUUID().toString().toUpperCase();
            PaymentResult result = paymentGatewayService.disburse(
                    loanRequest.getRequestedAmount(),
                    loanRequest.getBorrower(),
                    paymentReference
            );

            if (!result.isSuccessful()) {
                response.setStatusCode(400);
                response.setMessage("Payment gateway failed: " + result.getErrorMessage());
                return response;
            }

            // --- PERSIST ---
            transactionService.recordDisbursement(
                    admin, loanRequest.getBorrower(), loanRequest,
                    loanRequest.getRequestedAmount(), paymentReference);

            repaymentScheduleService.generateSchedule(loanRequest);

            loanRequest.setStatus(LoanStatus.ACTIVE);
            LoanRequest updatedLoan = loanRequestRepository.save(loanRequest);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Loan disbursed successfully. Reference: " + paymentReference);
            response.setData(Utils.mapLoanRequestEntityToDTO(updatedLoan));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error disbursing loan: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return response;
    }

}