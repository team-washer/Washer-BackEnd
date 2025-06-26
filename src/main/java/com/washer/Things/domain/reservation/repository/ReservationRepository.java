package com.washer.Things.domain.reservation.repository;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.reservation.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND r.status IN :statuses")
    List<Reservation> findActiveByRoomWithLock(@Param("roomId") Long roomId, @Param("statuses") List<Reservation.ReservationStatus> statuses);


    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.startTime < :time")
    List<Reservation> findAllByStatusAndStartTimeBefore(
            @Param("status") Reservation.ReservationStatus status,
            @Param("time") LocalDateTime time
    );

    List<Reservation> findAllByStatus(Reservation.ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.machine.id = :machineId AND r.status IN :statuses")
    List<Reservation> findByMachineIdAndStatusInWithLock(@Param("machineId") Long machineId,
                                                         @Param("statuses") List<Reservation.ReservationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.machine.id = :machineId AND r.status = :status ORDER BY r.createdAt ASC")
    Optional<Reservation> findFirstWaitingWithLock(@Param("machineId") Long machineId, @Param("status") Reservation.ReservationStatus status);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);

    Optional<Reservation> findFirstByRoomIdAndStatusInOrderByCreatedAtAsc(Long roomId, List<Reservation.ReservationStatus> statuses);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status IN :statuses " +
            "AND (:type IS NULL OR r.machine.type = :type) " +
            "AND (:floor IS NULL OR r.machine.floor = :floor) " +
            "ORDER BY r.createdAt ASC")
    List<Reservation> findByStatusesAndOptionalMachineFilters(
            @Param("statuses") List<Reservation.ReservationStatus> statuses,
            @Param("type") Machine.MachineType type,
            @Param("floor") Machine.Floor floor
    );

    Optional<Reservation> findTopByRoomIdAndStatusOrderByCompletedAtDesc(Long roomId, Reservation.ReservationStatus status);

    List<Reservation> findTop5ByMachineIdOrderByCreatedAtDesc(Long machineId);

}
