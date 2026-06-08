package com.finance.loan.utils;

import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.entity.RepaymentSchedule;

import java.util.List;
import java.util.stream.Collectors;

public class RepaymentScheduleUtils {

    public static List<RepaymentScheduleDTO> mapRepaymentScheduleListToOutput(List<RepaymentSchedule> schedules) {
        return schedules.stream()
                .map(RepaymentScheduleUtils::mapRepaymentScheduleEntityToOutput)
                .collect(Collectors.toList());
    }

    public static RepaymentScheduleDTO mapRepaymentScheduleEntityToOutput(RepaymentSchedule schedule) {
        RepaymentScheduleDTO dto = new RepaymentScheduleDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setInstallmentNumber(schedule.getInstallmentNumber());
        dto.setDueDate(schedule.getDueDate());
        dto.setAmountDue(schedule.getAmountDue());
        dto.setPrincipalComponent(schedule.getPrincipalComponent());
        dto.setInterestComponent(schedule.getInterestComponent());
        dto.setAmountPaid(schedule.getAmountPaid());
        dto.setStatus(schedule.getStatus());
        dto.setPaidDate(schedule.getPaidDate());
        return dto;
    }
}
