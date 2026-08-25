package com.finance.loan.repo;

import com.finance.loan.entity.PayoutAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PayoutAccountRepository extends JpaRepository<PayoutAccount, Long> {
    Optional<PayoutAccount> findByUserId(Long userId);

    boolean existsByUserIdAndAccountNumber(Long id, String normalized);

    Optional<PayoutAccount> findByUserIdAndAccountNumber(Long id, String normalized);

    @Modifying
    @Query("UPDATE PayoutAccount p SET p.isDefault = false WHERE p.user.id = :userId AND p.isDefault = true")
    void clearDefaultForUser(@Param("userId") Long userId);

    Optional<PayoutAccount> findByUserIdAndIsDefaultTrue(Long userId);
}