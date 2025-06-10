package com.washer.Things.domain.machine.service.Impl;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.entity.MachineReport;
import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;
import com.washer.Things.domain.machine.repository.MachineReportRepository;
import com.washer.Things.domain.machine.repository.MachineRepository;
import com.washer.Things.domain.machine.service.MachineErrorService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.global.util.UserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MachineErrorServiceImpl implements MachineErrorService {
    private final UserUtil userUtil;
    private final MachineReportRepository machineReportRepository;
    private final MachineRepository machineRepository;
    @Transactional
    public void reportMachineError(ReportMachineErrorRequest request) {
        User user = userUtil.getCurrentUser();

        Machine machine = machineRepository.findByName(request.getMachineName())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 이름의 기기가 존재하지 않습니다."));

        MachineReport report = MachineReport.builder()
                .machine(machine.getName())
                .reportedByUserName(user.getName())
                .reportedByUserNumber(user.getSchoolNumber())
                .description(request.getDescription())
                .status(MachineReport.ReportStatus.pending)
                .build();

        machineReportRepository.save(report);
    }
}
