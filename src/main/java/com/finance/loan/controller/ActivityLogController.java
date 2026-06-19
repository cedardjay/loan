package com.finance.loan.controller;

import com.finance.loan.dto.output.ActivityLogDTO;
import com.finance.loan.service.implementation.ActivityLogService;
import com.finance.loan.service.interfac.IActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity-log")
public class ActivityLogController {

    @Autowired
    private IActivityLogService activityLogService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getAllActivityLogs() {
        return ResponseEntity.ok(activityLogService.getAllActivityLogs());
    }

    @GetMapping("/my-logs")
    public ResponseEntity<List<ActivityLogDTO>> getMyActivityLogs() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(activityLogService.getMyActivityLogs(email));
    }
}
