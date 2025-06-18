package com.washer.Things.domain.machine.service;

import com.washer.Things.domain.machine.entity.MachineReport;
import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;
import com.washer.Things.domain.machine.presentation.dto.response.MachineReportResponse;

import java.util.List;

public interface MachineErrorService {
    void reportMachineError(ReportMachineErrorRequest request);
    List<MachineReportResponse> getAllReports();
    void updateReportStatus(Long reportId, MachineReport.ReportStatus status);
}
