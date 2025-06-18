package com.washer.Things.domain.reservation.presentation;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.reservation.presentation.dto.response.AdminReservationResponse;
import com.washer.Things.domain.reservation.service.ReservationAdminService;
import com.washer.Things.domain.reservation.service.ReservationService;
import com.washer.Things.global.exception.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/reservation")
@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationAdminService reservationAdminService;
    @PostMapping("/{machineId}")
    public ResponseEntity<ApiResponse<Void>> createReservation(@PathVariable Long machineId) {
        reservationService.createReservation(machineId);
        return ResponseEntity.ok(ApiResponse.success("예약 성공"));
    }

    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReservation(@PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 수락 성공"));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 삭제 성공"));
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<ApiResponse<List<AdminReservationResponse>>> getAllActiveReservations(
            @RequestParam(required = false) Machine.MachineType type,
            @RequestParam(required = false) Machine.Floor floor
    ) {
        List<AdminReservationResponse> result = reservationAdminService.getAllActiveReservations(type, floor);
        return ResponseEntity.ok(ApiResponse.success(result, "현재 활성 예약 조회 성공"));
    }

    @DeleteMapping("/admin/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> cancelReservationByAdmin(@PathVariable Long reservationId) {
        reservationAdminService.cancelReservationByAdmin(reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 강제 취소 성공"));
    }
}
