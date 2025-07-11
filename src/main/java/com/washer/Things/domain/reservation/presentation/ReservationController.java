package com.washer.Things.domain.reservation.presentation;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.reservation.presentation.dto.response.AdminReservationResponse;
import com.washer.Things.domain.reservation.presentation.dto.response.ReservationHistoryResponse;
import com.washer.Things.domain.reservation.service.ReservationAdminService;
import com.washer.Things.domain.reservation.service.ReservationService;
import com.washer.Things.global.exception.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@RequestMapping("/reservation")
@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationAdminService reservationAdminService;
    @Operation(summary = "예약 하기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 성공"),
            @ApiResponse(responseCode = "400", description = "호실에서 다른 기기를 사용중 / 기기가 고장상태"),
            @ApiResponse(responseCode = "403", description = "정지된 사용자"),
            @ApiResponse(responseCode = "404", description = "기기를 찾을 수 없는 경우")
    })
    @PostMapping("/{machineId}")
    public ResponseEntity<BaseResponse<Void>> createReservation(@PathVariable Long machineId) {
        reservationService.createReservation(machineId);
        return ResponseEntity.ok(BaseResponse.success("예약 성공"));
    }

    @Operation(summary = "예약 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 수락 성공"),
            @ApiResponse(responseCode = "400", description = "예약 상태가 시작 가능 상태가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없는 경우")
    })
    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<BaseResponse<Void>> confirmReservation(@PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
        return ResponseEntity.ok(BaseResponse.success("예약 수락 성공"));
    }

    @Operation(summary = "예약 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "이미 취소되었거나 완료된 예약"),
            @ApiResponse(responseCode = "403", description = "해당 예약을 취소할 권한이 없음"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없는 경우")
    })
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<BaseResponse<Void>> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(BaseResponse.success("예약 삭제 성공"));
    }

    @Operation(summary = "전체 예약 보기 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현재 활성 예약 조회 성공")
    })
    @GetMapping("/admin/reservations")
    public ResponseEntity<BaseResponse<List<AdminReservationResponse>>> getAllActiveReservations(
            @Parameter(description = "필터: 기기 타입") @RequestParam(required = false) Machine.MachineType type,
            @Parameter(description = "필터: 층") @RequestParam(required = false) Machine.Floor floor
    ) {
        List<AdminReservationResponse> result = reservationAdminService.getAllActiveReservations(type, floor);
        return ResponseEntity.ok(BaseResponse.success(result, "현재 활성 예약 조회 성공"));
    }

    @Operation(summary = "예약 강제 삭제 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 강제 취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 취소되었거나 완료된 예약"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없는 경우")
    })
    @DeleteMapping("/admin/{reservationId}")
    public ResponseEntity<BaseResponse<Void>> cancelReservationByAdmin(@PathVariable Long reservationId) {
        reservationAdminService.cancelReservationByAdmin(reservationId);
        return ResponseEntity.ok(BaseResponse.success("예약 강제 취소 성공"));
    }

    @Operation(summary = "기기 히스토리 보기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기기 히스토리 조회 성공")
    })
    @GetMapping("/machine/{machineId}/history")
    public ResponseEntity<BaseResponse<List<ReservationHistoryResponse>>> getMachineHistory(@PathVariable Long machineId) {
        List<ReservationHistoryResponse> history = reservationService.getReservationHistory(machineId);
        return ResponseEntity.ok(BaseResponse.success(history, "기기 히스토리 조회 성공"));
    }
}
