package com.finance.loan.repo;

import com.finance.loan.entity.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
    Optional<PaymentAccount> findByUserId(Long userId);

    boolean existsByUserIdAndAccountNumber(Long id, String normalized);

    Optional<PaymentAccount> findByUserIdAndAccountNumber(Long id, String normalized);

    @Modifying
    @Query("UPDATE PaymentAccount p SET p.isDefault = false WHERE p.user.id = :userId AND p.isDefault = true")
    void clearDefaultForUser(@Param("userId") Long userId);

    Optional<PaymentAccount> findByUserIdAndIsDefaultTrue(Long userId);
}