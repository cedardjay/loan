package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PaymentAccountDTO;
import com.finance.loan.entity.PaymentAccount;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.PaymentAccountRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.IPayoutAccountService;
import com.finance.loan.utils.PaymentAccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentAccountService implements IPayoutAccountService {

    @Autowired
    private PaymentAccountRepository payoutAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public PaymentAccountDTO createPayoutAccount(PayoutAccountIN requestDTO, String email) {
        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        String normalized = normalizeAccountNumber(requestDTO.getAccountNumber());

        Optional<PaymentAccount> existing = payoutAccountRepository.findByUserIdAndAccountNumber(user.getId(), normalized);

        if (existing.isPresent()) {
            // --- EXECUTE (unset old default, set this one) ---
            payoutAccountRepository.clearDefaultForUser(user.getId());
            PaymentAccount account = existing.get();
            account.setIsDefault(true);

            // --- PERSIST ---
            PaymentAccount saved = payoutAccountRepository.save(account);

            // --- RETURN ---
            return PaymentAccountUtils.mapEntityToOutput(saved);
        }

        // --- VALIDATE ---

            if (!normalized.matches("^237\\d{9}$")) {
                throw new OurException("Account number must be in format 237XXXXXXXXX or +237XXXXXXXXX", 400);
            }



        // --- EXECUTE ---
        payoutAccountRepository.clearDefaultForUser(user.getId());

        PaymentAccount payoutAccount = new PaymentAccount();
        payoutAccount.setUser(user);
        payoutAccount.setPaymentMethod(requestDTO.getPaymentMethod());
        payoutAccount.setAccountNumber(normalized);
        payoutAccount.setIsDefault(true);

        // --- PERSIST ---
        PaymentAccount saved = payoutAccountRepository.save(payoutAccount);

        // --- RETURN ---
        return PaymentAccountUtils.mapEntityToOutput(saved);
    }

    private String normalizeAccountNumber(String accountNumber) {
        return accountNumber.startsWith("+") ? accountNumber.substring(1) : accountNumber;
    }

    //get default payout account
    public PaymentAccountDTO getDefaultPayoutAccount(String email) {

        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        PaymentAccount payoutAccount = payoutAccountRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new OurException("No default payout account found", 404));

        // --- RETURN ---
        return PaymentAccountUtils.mapEntityToOutput(payoutAccount);
    }


    // GET PAYOUT METHOD
    public PaymentAccountDTO getPayoutAccount(String email) {

        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        PaymentAccount payoutAccount = payoutAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new OurException("Payout Account not found", 404));

        // --- RETURN ---
        return PaymentAccountUtils.mapEntityToOutput(payoutAccount);
    }



    // GET PAYOUT METHOD BY USER ID (ADMIN)
    public PaymentAccountDTO getPayoutAccountByUserId(Long userId) {

        // --- FETCH ---
        userRepository.findById(userId)
                .orElseThrow(() -> new OurException("User not found", 404));

        PaymentAccount payoutAccount = payoutAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new OurException("Payout method not found", 404));

        // --- RETURN ---
        return PaymentAccountUtils.mapEntityToOutput(payoutAccount);
    }
}