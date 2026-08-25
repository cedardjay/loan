package com.finance.loan.controller;


import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PayoutAccountDTO;
import com.finance.loan.service.interfac.*;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payout-accounts")
public class PayoutAccountController {

    @Autowired
    IPayoutAccountService payoutAccountService;

    // create PAYOUT Account
    @PostMapping
    public ResponseEntity<PayoutAccountDTO> createPayoutAccount(@Valid @RequestBody PayoutAccountIN payoutMethodIN ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(payoutAccountService.createPayoutAccount(payoutMethodIN,email));
    }
//get payout accounts
    @GetMapping
    public ResponseEntity<PayoutAccountDTO> getPayoutAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(payoutAccountService.getPayoutAccount(email));
    }

    @GetMapping("/default")
    public ResponseEntity<PayoutAccountDTO> getDefaultPayoutAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(payoutAccountService.getDefaultPayoutAccount(email));
    }

//get by id
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PayoutAccountDTO> getPayoutAccountByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(payoutAccountService.getPayoutAccountByUserId(userId));
    }

    @PostMapping("/platform")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PayoutAccountDTO> setPlatformPayoutAccount(
            @Valid @RequestBody PayoutAccountIN requestDTO) {

        PayoutAccountDTO response = payoutAccountService.createPayoutAccount(requestDTO, "platform@system.internal");
        return ResponseEntity.ok(response);
    }


}


