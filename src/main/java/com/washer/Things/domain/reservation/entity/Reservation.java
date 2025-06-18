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
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Machine.MachineType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;        // 예약 시작 예정 시간(언제까지 시작할게요를 눌러야한다)  reserved

    private LocalDateTime pausedSince;      // 유저가 기기 잠시 멈추었을 때
    private LocalDateTime confirmedAt;      // 사용자 예약 확인 시간(시작할게요 누른 시간)  confirmed
    private LocalDateTime startedAt;        // 실제 세탁/건조 시작 시간  running
    private LocalDateTime completedAt;      // 세탁/건조 완료 시간  completed
    private LocalDateTime cancelledAt;      // 예약 취소 시간  cancelled

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum ReservationStatus {
        waiting, reserved, confirmed, running, cancelled, completed
    }
    /**
     * - waiting: 앞 호실 세탁/건조 완료 대기중
     * - reserved: 예약됨
     * - confirmed: 예약 확정 (지금 사용)
     * - running: 세탁/건조 진행 중
     * - cancelled: 예약 취소됨
     * - completed: 세탁/건조 완료됨
     */
}
