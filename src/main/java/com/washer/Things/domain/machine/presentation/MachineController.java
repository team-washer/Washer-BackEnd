package com.washer.Things.domain.machine.presentation;

import com.washer.Things.domain.machine.entity.MachineReport;
import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;
import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;
import com.washer.Things.domain.machine.presentation.dto.response.MachineReportResponse;
import com.washer.Things.domain.machine.service.MachineErrorService;
import com.washer.Things.domain.machine.service.MachineInfoService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequiredArgsConstructor
@RequestMapping("/machine")
public class MachineController {
    private final MachineInfoService machineInfoService;
    private final MachineErrorService machineErrorService;
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<Map<String, List<DeviceInfoResponse>>>> getMyDevices(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String floor
    ) {
        Map<String, List<DeviceInfoResponse>> devices = machineInfoService.getMyDevices(type, floor);
        return ResponseEntity.ok(ApiResponse.success(devices, "SmartThings 디바이스 조회 성공"));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> reportMachineError(ReportMachineErrorRequest request) {
        machineErrorService.reportMachineError(request);
        return ResponseEntity.ok(ApiResponse.success("신고 접수 성공"));
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<ApiResponse<List<MachineReportResponse>>> getAllReports() {
        List<MachineReportResponse> reports = machineErrorService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success(reports, "기기 고장 신고 목록 조회 성공"));
    }

    @PatchMapping("/admin/reports/{reportId}")
    public ResponseEntity<ApiResponse<Void>> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam MachineReport.ReportStatus status
    ) {
        machineErrorService.updateReportStatus(reportId, status);
        return ResponseEntity.ok(ApiResponse.success("신고 상태 업데이트 성공"));
    }

}
