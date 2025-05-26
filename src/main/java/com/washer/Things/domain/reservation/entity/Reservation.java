package com.washer.Things.domain.reservation.entity;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.room.entity.Room;
import com.washer.Things.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "reservation")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_number", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Machine.MachineType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private int timeRemaining;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime confirmedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime collectedAt;
    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private int connectionAttempts = 0;

    public enum ReservationStatus {
        reserved, confirmed, running, collection, cancelled, completed, connecting
    }
}
