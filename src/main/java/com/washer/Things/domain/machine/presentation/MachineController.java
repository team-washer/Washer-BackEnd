package com.washer.Things.domain.machine.presentation;

import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;
import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;
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
}
