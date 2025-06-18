package com.washer.Things.domain.reservation.service;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.reservation.presentation.dto.response.AdminReservationResponse;

import java.util.List;

public interface ReservationAdminService {
    List<AdminReservationResponse> getAllActiveReservations(Machine.MachineType type, Machine.Floor floor);

    void cancelReservationByAdmin(Long reservationId);
}
