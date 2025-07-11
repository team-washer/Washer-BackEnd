package com.washer.Things.domain.machine.presentation.dto.response;

import com.washer.Things.domain.machine.entity.MachineReport;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Builder
@Getter
public class MachineReportResponse {
    private Long reportId;
    private String machineName;
    private String reportedByUserName;
    private String reportedByUserNumber;
    private String description;
    private String status;
    private LocalDateTime resolvedAt;
    public static MachineReportResponse from(MachineReport report) {
        return MachineReportResponse.builder()
                .reportId(report.getId())
                .machineName(report.getMachine().getName())
                .reportedByUserName(report.getReportedBy().getName())
                .reportedByUserNumber(report.getReportedBy().getSchoolNumber())
                .description(report.getDescription())
                .status(report.getStatus().name())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
