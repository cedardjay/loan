package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.ActivityLogDTO;

import java.util.List;

public interface IActivityLogService {
    List<ActivityLogDTO> getAllActivityLogs();
    List<ActivityLogDTO> getMyActivityLogs(String email);
}
