package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PayoutAccountDTO;
import com.finance.loan.entity.PayoutAccount;
import com.finance.loan.entity.PayoutType;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.PayoutAccountRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.IPayoutAccountService;
import com.finance.loan.utils.PayoutAccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PayoutAccountService implements IPayoutAccountService {

    @Autowired
    private PayoutAccountRepository payoutAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public PayoutAccountDTO createPayoutAccount(PayoutAccountIN requestDTO, String email) {
        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        String normalized = normalizeAccountNumber(requestDTO.getAccountNumber());

        Optional<PayoutAccount> existing = payoutAccountRepository.findByUserIdAndAccountNumber(user.getId(), normalized);

        if (existing.isPresent()) {
            // --- EXECUTE (unset old default, set this one) ---
            payoutAccountRepository.clearDefaultForUser(user.getId());
            PayoutAccount account = existing.get();
            account.setIsDefault(true);

            // --- PERSIST ---
            PayoutAccount saved = payoutAccountRepository.save(account);

            // --- RETURN ---
            return PayoutAccountUtils.mapEntityToOutput(saved);
        }

        // --- VALIDATE ---

            if (!normalized.matches("^237\\d{9}$")) {
                throw new OurException("Account number must be in format 237XXXXXXXXX or +237XXXXXXXXX", 400);
            }



        // --- EXECUTE ---
        payoutAccountRepository.clearDefaultForUser(user.getId());

        PayoutAccount payoutAccount = new PayoutAccount();
        payoutAccount.setUser(user);
        payoutAccount.setType(requestDTO.getType());
        payoutAccount.setAccountNumber(normalized);
        payoutAccount.setIsDefault(true);

        // --- PERSIST ---
        PayoutAccount saved = payoutAccountRepository.save(payoutAccount);

        // --- RETURN ---
        return PayoutAccountUtils.mapEntityToOutput(saved);
    }

    private String normalizeAccountNumber(String accountNumber) {
        return accountNumber.startsWith("+") ? accountNumber.substring(1) : accountNumber;
    }

    //get default payout account
    public PayoutAccountDTO getDefaultPayoutAccount(String email) {

        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        PayoutAccount payoutAccount = payoutAccountRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new OurException("No default payout account found", 404));

        // --- RETURN ---
        return PayoutAccountUtils.mapEntityToOutput(payoutAccount);
    }


    // GET PAYOUT METHOD
    public PayoutAccountDTO getPayoutAccount(String email) {

        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        PayoutAccount payoutAccount = payoutAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new OurException("Payout Account not found", 404));

        // --- RETURN ---
        return PayoutAccountUtils.mapEntityToOutput(payoutAccount);
    }



    // GET PAYOUT METHOD BY USER ID (ADMIN)
    public PayoutAccountDTO getPayoutAccountByUserId(Long userId) {

        // --- FETCH ---
        userRepository.findById(userId)
                .orElseThrow(() -> new OurException("User not found", 404));

        PayoutAccount payoutAccount = payoutAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new OurException("Payout method not found", 404));

        // --- RETURN ---
        return PayoutAccountUtils.mapEntityToOutput(payoutAccount);
    }
}