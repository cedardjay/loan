package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.output.LoanRequestOUT;
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

    // CREATE
    public Response createLoanRequest(LoanRequestIN requestDTO, String email) {
        Response response = new Response();
        try {
            User borrower = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setBorrower(borrower);
            loanRequest.setRequestedAmount(requestDTO.getRequestedAmount());
            loanRequest.setDescription(requestDTO.getDescription());
            loanRequest.setTermMonths(requestDTO.getTermMonths());
            loanRequest.setPurpose(requestDTO.getPurpose());
            loanRequest.setAmountFunded(BigDecimal.ZERO);
            loanRequest.setStatus(LoanStatus.PENDING_APPROVAL);
            loanRequest.setRequestDate(LocalDateTime.now());

            BigDecimal interestRate = LoanCalculatorUtil.calculateInterestRate(requestDTO.getRequestedAmount(), requestDTO.getTermMonths());
            loanRequest.setInterestRate(interestRate);

            LoanRequest savedRequest = loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Loan request created successfully");
            response.setLoanrequest(Utils.mapLoanRequestEntityToOutput(savedRequest));

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
    public Response getLoanRequestsByBorrowerEmail(String email) {
        Response response = new Response();
        try {
            List<LoanRequest> loanRequests = loanRequestRepository.findByBorrowerEmail(email);
            List<LoanRequestOUT> loanRequestOuts = Utils.mapLoanRequestListEntityToListOutput(loanRequests);

            response.setStatusCode(200);
            response.setMessage("User's loan requests fetched successfully");
            response.setLoanrequestlist(loanRequestOuts);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching user's loan requests: " + e.getMessage());
        }
        return response;
    }

    // GET MY REQUEST BY ID
    public Response getMyLoanRequestById(Long requestId, String email) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            if (!loanRequest.getBorrower().getEmail().equals(email)) {
                response.setStatusCode(403);
                response.setMessage("You are not authorized to view this loan request");
                return response;
            }

            response.setStatusCode(200);
            response.setMessage("Loan request fetched successfully");
            response.setLoanrequest(Utils.mapLoanRequestEntityToOutput(loanRequest));

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
    public Response deleteLoanRequest(Long requestId, String email) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            if (!loanRequest.getBorrower().getEmail().equals(email)) {
                response.setStatusCode(403);
                response.setMessage("You are not authorized to delete this loan request");
                return response;
            }

            if (loanRequest.getStatus() != LoanStatus.FULLY_FUNDED &&
                    loanRequest.getAmountFunded().compareTo(BigDecimal.ZERO) == 0) {
                loanRequestRepository.delete(loanRequest);
                response.setStatusCode(200);
                response.setMessage("Loan request deleted successfully");
            } else {
                response.setStatusCode(400);
                response.setMessage("Cannot delete loan request that has been funded");
            }

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting loan request: " + e.getMessage());
        }
        return response;
    }


    // GET ALL loan requests (ADMIN and SUPERADMINS)
    public Response getAllLoanRequests() {
        Response response = new Response();
        try {
            List<LoanRequest> loanRequests = loanRequestRepository.findAll();
            List<LoanRequestOUT> loanRequestOuts = Utils.mapLoanRequestListEntityToListOutput(loanRequests);

            response.setStatusCode(200);
            response.setMessage("Loan requests fetched successfully");
            response.setLoanrequestlist(loanRequestOuts);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan requests: " + e.getMessage());
        }
        return response;
    }


    // GET BY request ID (ADMIN and SUPERADMINS)
    public Response getLoanRequestById(Long requestId) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            response.setStatusCode(200);
            response.setMessage("Loan request fetched successfully");
            response.setLoanrequest(Utils.mapLoanRequestEntityToOutput(loanRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan request: " + e.getMessage());
        }
        return response;
    }


    // GET LOAN REQUESTS BY BORROWER ID (ADMIN and SUPERADMINS)
    public Response getLoanRequestsByBorrowerId(Long borrowerId) {
        Response response = new Response();
        try {
            User borrower = userRepository.findById(borrowerId)
                    .orElseThrow(() -> new OurException("User not found with id: " + borrowerId));

            List<LoanRequest> loanRequests = loanRequestRepository.findByBorrower(borrower);
            List<LoanRequestOUT> loanRequestOuts = Utils.mapLoanRequestListEntityToListOutput(loanRequests);

            response.setStatusCode(200);
            response.setMessage("Loan requests fetched successfully");
            response.setLoanrequestlist(loanRequestOuts);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan requests: " + e.getMessage());
        }
        return response;
    }

    // APPROVE Loan Request (ADMIN / SUPERADMIN)
    public Response approveLoanRequest(Long requestId) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
                response.setStatusCode(400);
                response.setMessage("Only pending loan requests can be approved");
                return response;
            }

            loanRequest.setStatus(LoanStatus.APPROVED);
            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Loan request approved successfully");
            response.setLoanrequest(Utils.mapLoanRequestEntityToOutput(updatedRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error approving loan request: " + e.getMessage());
        }
        return response;
    }

    // REJECT Loan Request (ADMIN / SUPERADMIN)
    public Response rejectLoanRequest(Long requestId) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
                response.setStatusCode(400);
                response.setMessage("Only pending loan requests can be rejected");
                return response;
            }

            loanRequest.setStatus(LoanStatus.REJECTED);
            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Loan request rejected successfully");
            response.setLoanrequest(Utils.mapLoanRequestEntityToOutput(updatedRequest));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error rejecting loan request: " + e.getMessage());
        }
        return response;
    }

    // GET MARKETPLACE LOANS (APPROVED, PARTIALLY_FUNDED, FULLY_FUNDED)
    public Response getMarketplaceLoans() {
        Response response = new Response();
        try {
            List<LoanStatus> marketplaceStatuses = List.of(
                    LoanStatus.APPROVED,
                    LoanStatus.PARTIALLY_FUNDED,
                    LoanStatus.FULLY_FUNDED
            );

            List<LoanRequest> loanRequests = loanRequestRepository.findByStatusIn(marketplaceStatuses);
            List<LoanRequestOUT> loanRequestOuts = Utils.mapLoanRequestListEntityToListOutput(loanRequests);

            response.setStatusCode(200);
            response.setMessage("Marketplace loans fetched successfully");
            response.setLoanrequestlist(loanRequestOuts);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching marketplace loans: " + e.getMessage());
        }
        return response;
    }

    //DISBURSING A LOAN
    @Transactional
    public Response disburseLoan(Long requestId, String adminEmail) {
        Response response = new Response();
        try {

            // --- FETCH ---
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new OurException("Admin user not found"));

            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId));

            // --- VALIDATE ---
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
            response.setLoanrequest(Utils.mapLoanRequestEntityToOutput(updatedLoan));

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

/*
    // GET Loan Requests by Status
    public Response getLoanRequestsByStatus(LoanStatus status) {
        Response response = new Response();
        try {
            List<LoanRequest> loanRequests = loanRequestRepository.findByStatus(status);
            List<LoanRequestOut> loanRequestOuts = Utils.mapLoanRequestListEntityToListOutput(loanRequests);

            response.setStatusCode(200);
            response.setMessage("Loan requests by status fetched successfully");
            response.setLoanrequestlist(loanRequestOuts);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching loan requests by status: " + e.getMessage());
        }
        return response;
    }



    // UPDATE Loan Request Status
    public Response updateLoanRequestStatus(Long requestId, LoanStatus newStatus) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            loanRequest.setStatus(newStatus);

            // If fully funded, update state to approved
            if (newStatus == LoanStatus.FULLY_FUNDED) {
                loanRequest.setState(LoanState.APPROVED);
            }

            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Loan request status updated successfully");
            LoanRequestOut loanRequestOut = Utils.mapLoanRequestEntityToOutput(updatedRequest);
            response.setLoanrequest(loanRequestOut);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating loan request status: " + e.getMessage());
        }
        return response;
    }


*/




}