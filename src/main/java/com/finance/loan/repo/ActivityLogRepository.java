package com.finance.loan.repo;

import com.finance.loan.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<ActivityLog> findByActorEmail(String actorEmail);
}
