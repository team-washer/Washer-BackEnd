package com.washer.Things.domain.reservation.service;

public interface ReservationService {
    void createReservation(Long machineId);

    void confirmReservation(Long reservationId);

    void cancelReservation(Long reservationId);
}
