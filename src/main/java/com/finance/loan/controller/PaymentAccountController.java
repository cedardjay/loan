package com.finance.loan.controller;


import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PaymentAccountDTO;
import com.finance.loan.service.interfac.*;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payout-accounts")
public class PaymentAccountController {

    @Autowired
    IPayoutAccountService payoutAccountService;

    // create PAYOUT Account
    @PostMapping
    public ResponseEntity<PaymentAccountDTO> createPayoutAccount(@Valid @RequestBody PayoutAccountIN payoutMethodIN ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(payoutAccountService.createPayoutAccount(payoutMethodIN,email));
    }
//get payout accounts
    @GetMapping
    public ResponseEntity<PaymentAccountDTO> getPayoutAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(payoutAccountService.getPayoutAccount(email));
    }

    @GetMapping("/default")
    public ResponseEntity<PaymentAccountDTO> getDefaultPayoutAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(payoutAccountService.getDefaultPayoutAccount(email));
    }

//get by id
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PaymentAccountDTO> getPayoutAccountByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(payoutAccountService.getPayoutAccountByUserId(userId));
    }

    @PostMapping("/platform")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PaymentAccountDTO> setPlatformPayoutAccount(
            @Valid @RequestBody PayoutAccountIN requestDTO) {

        PaymentAccountDTO response = payoutAccountService.createPayoutAccount(requestDTO, "platform@system.internal");
        return ResponseEntity.ok(response);
    }


}


