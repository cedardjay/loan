package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.ActivityLogDTO;
import com.finance.loan.repo.ActivityLogRepository;
import com.finance.loan.service.interfac.IActivityLogService;
import com.finance.loan.utils.ActivityLogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService implements IActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Override
    public List<ActivityLogDTO> getAllActivityLogs() {
        return ActivityLogUtils.mapActivityLogListToOutput(activityLogRepository.findAll());
    }
    @Override
    public List<ActivityLogDTO> getMyActivityLogs(String email) {
        return ActivityLogUtils.mapActivityLogListToOutput(activityLogRepository.findByActorEmail(email));
    }
}
