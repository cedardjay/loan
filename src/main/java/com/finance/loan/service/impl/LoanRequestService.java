package com.finance.loan.service.impl;

import com.finance.loan.dto.LoanRequestIN;
import com.finance.loan.dto.LoanRequestOUT;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.LoanStatus;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.dto.Response;
import com.finance.loan.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LoanRequestService {

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private UserRepository userRepository;

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

            BigDecimal interestRate = calculateInterestRate(requestDTO.getRequestedAmount(), requestDTO.getTermMonths());
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

    private BigDecimal calculateInterestRate(BigDecimal amount, Integer termMonths) {
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return new BigDecimal("5.5");
        } else if (termMonths > 24) {
            return new BigDecimal("6.0");
        } else {
            return new BigDecimal("7.0");
        }
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


    // GET ALL (ADMIN and SUPERADMINS)
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

    // UPDATE Loan Request State
    public Response updateLoanRequestState(Long requestId, LoanState newState) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            loanRequest.setState(newState);
            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Loan request state updated successfully");
            LoanRequestOut loanRequestOut = Utils.mapLoanRequestEntityToOutput(updatedRequest);
            response.setLoanrequest(loanRequestOut);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating loan request state: " + e.getMessage());
        }
        return response;
    }

    // UPDATE Amount Funded
    public Response updateAmountFunded(Long requestId, BigDecimal additionalAmount) {
        Response response = new Response();
        try {
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            BigDecimal newAmountFunded = loanRequest.getAmountFunded().add(additionalAmount);

            // Check if fully funded
            if (newAmountFunded.compareTo(loanRequest.getRequestedAmount()) >= 0) {
                newAmountFunded = loanRequest.getRequestedAmount();
                loanRequest.setStatus(LoanStatus.FULLY_FUNDED);
                loanRequest.setState(LoanState.APPROVED);
            }

            loanRequest.setAmountFunded(newAmountFunded);
            LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Amount funded updated successfully");
            LoanRequestOut loanRequestOut = Utils.mapLoanRequestEntityToOutput(updatedRequest);
            response.setLoanrequest(loanRequestOut);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating amount funded: " + e.getMessage());
        }
        return response;
    }
*/




}