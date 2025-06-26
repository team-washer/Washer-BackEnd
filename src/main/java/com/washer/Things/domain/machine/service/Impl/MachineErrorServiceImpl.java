package com.washer.Things.domain.machine.service.Impl;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.entity.MachineReport;
import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;
import com.washer.Things.domain.machine.presentation.dto.response.MachineReportResponse;
import com.washer.Things.domain.machine.repository.MachineReportRepository;
import com.washer.Things.domain.machine.repository.MachineRepository;
import com.washer.Things.domain.machine.service.MachineErrorService;
import com.washer.Things.domain.user.entity.User;
import com.washer.Things.global.exception.HttpException;
import com.washer.Things.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineErrorServiceImpl implements MachineErrorService {
    private final UserService userService;
    private final MachineReportRepository machineReportRepository;
    private final MachineRepository machineRepository;
    @Transactional
    public void reportMachineError(ReportMachineErrorRequest request) {
        User user = userService.getCurrentUser();

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

    @Transactional
    public List<MachineReportResponse> getAllReports() {
        return machineReportRepository.findAllByOrderByIdDesc()
                .stream()
                .map(MachineReportResponse::from)
                .toList();
    }
    @Transactional
    public void updateReportStatus(Long reportId, MachineReport.ReportStatus status) {
        MachineReport report = machineReportRepository.findById(reportId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "신고 내역이 존재하지 않습니다."));

        report.setStatus(status);

        machineRepository.findByName(report.getMachine()).ifPresent(machine -> {
            if (status == MachineReport.ReportStatus.in_progress) {
                machine.setOutOfOrder(true);
            } else if (status == MachineReport.ReportStatus.resolved) {
                machine.setOutOfOrder(false);
                report.setResolvedAt(LocalDateTime.now());
            }
        });
    }
}
