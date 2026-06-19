package com.finance.loan.utils;

import com.finance.loan.dto.output.ActivityLogDTO;
import com.finance.loan.entity.ActivityLog;

import java.util.List;

public class ActivityLogUtils {

    public static ActivityLogDTO mapActivityLogEntityToOutput(ActivityLog log) {
        return ActivityLogDTO.builder()
                .id(log.getId())
                .actorEmail(log.getActorEmail())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }

    public static List<ActivityLogDTO> mapActivityLogListToOutput(List<ActivityLog> logs) {
        return logs.stream()
                .map(ActivityLogUtils::mapActivityLogEntityToOutput)
                .toList();
    }
}
