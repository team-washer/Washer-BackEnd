package com.washer.Things.domain.machine.presentation;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.entity.MachineReport;
import com.washer.Things.domain.machine.presentation.dto.request.MachineOutOfOrderRequest;
import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;
import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;
import com.washer.Things.domain.machine.presentation.dto.response.MachineOutOfOrderResponse;
import com.washer.Things.domain.machine.presentation.dto.response.MachineReportResponse;
import com.washer.Things.domain.machine.service.MachineErrorService;
import com.washer.Things.domain.machine.service.MachineInfoService;
import com.washer.Things.domain.machine.service.MachineOutOfOrderService;
import com.washer.Things.global.exception.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final MachineOutOfOrderService machineOutOfOrderService;
    @Operation(summary = "기기, 예약 전체 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SmartThings 디바이스 조회 성공"),
            @ApiResponse(responseCode = "500", description = "기기 정보 로딩 실패 또는 서버 오류")
    })
    @GetMapping("/devices")
    public ResponseEntity<BaseResponse<Map<String, List<DeviceInfoResponse>>>> getMyDevices(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String floor
    ) {
        Map<String, List<DeviceInfoResponse>> devices = machineInfoService.getMyDevices(type, floor);
        return ResponseEntity.ok(BaseResponse.success(devices, "SmartThings 디바이스 조회 성공"));
    }
    @Operation(summary = "기기 고장 신고")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신고 접수 성공"),
            @ApiResponse(responseCode = "400", description = "해당 이름의 기기가 존재하지 않을 경우")
    })
    @PostMapping("/report")
    public ResponseEntity<BaseResponse<Void>> reportMachineError(@RequestBody ReportMachineErrorRequest request) {
        machineErrorService.reportMachineError(request);
        return ResponseEntity.ok(BaseResponse.success("신고 접수 성공"));
    }

    @Operation(summary = "기기 고장 신고 확인 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기기 고장 신고 목록 조회 성공")
    })
    @GetMapping("/admin/reports")
    public ResponseEntity<BaseResponse<List<MachineReportResponse>>> getAllReports() {
        List<MachineReportResponse> reports = machineErrorService.getAllReports();
        return ResponseEntity.ok(BaseResponse.success(reports, "기기 고장 신고 목록 조회 성공"));
    }

    @Operation(summary = "기기 고장 신고 상태 변경 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신고 상태 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "신고 내역이 존재하지 않을 경우")
    })
    @PatchMapping("/admin/reports/{reportId}")
    public ResponseEntity<BaseResponse<Void>> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam MachineReport.ReportStatus status
    ) {
        machineErrorService.updateReportStatus(reportId, status);
        return ResponseEntity.ok(BaseResponse.success("신고 상태 업데이트 성공"));
    }

    @Operation(summary = "기기 고장 여부 보기 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기기 고장 여부 목록 조회 성공")
    })
    @GetMapping("/admin/out-of-order")
    public ResponseEntity<BaseResponse<List<MachineOutOfOrderResponse>>> getMachines(
            @Parameter(description = "기기 타입 필터") @RequestParam(required = false) Machine.MachineType type,
            @Parameter(description = "층 정보 필터") @RequestParam(required = false) Machine.Floor floor
    ) {
        List<MachineOutOfOrderResponse> result = machineOutOfOrderService.getMachines(type, floor);
        return ResponseEntity.ok(BaseResponse.success(result, "기기 고장 여부 목록 조회 성공"));
    }

    @Operation(summary = "기기 고장 상태 변경 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기기 고장 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "해당 기기가 존재하지 않을 경우")
    })
    @PatchMapping("/admin/out-of-order")
    public ResponseEntity<BaseResponse<Void>> updateMachineOutOfOrderStatus(@RequestBody MachineOutOfOrderRequest request) {
        machineOutOfOrderService.updateMachineStatus(request);
        return ResponseEntity.ok(BaseResponse.success("기기 고장 상태 변경 성공"));
    }
}
