package com.washer.Things.domain.machine.presentation;

import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;
import com.washer.Things.domain.machine.service.MachineService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
@RestController
@RequiredArgsConstructor
@RequestMapping("/machine")
public class MachineController {
    private final MachineService machineService;
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<Map<String, List<DeviceInfoResponse>>>> getMyDevices(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String floor
    ) {
        Map<String, List<DeviceInfoResponse>> devices = machineService.getMyDevices(type, floor);
        return ResponseEntity.ok(ApiResponse.success(devices, "SmartThings 디바이스 조회 성공"));
    }
}
