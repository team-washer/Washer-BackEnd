package com.washer.Things.domain.reservation.service;

import com.washer.Things.domain.reservation.presentation.dto.response.ReservationHistoryResponse;

import java.util.List;

public interface ReservationService {
    void createReservation(Long machineId);

    void confirmReservation(Long reservationId);

    void cancelReservation(Long reservationId);

    List<ReservationHistoryResponse> getReservationHistory(Long machineId);
}
