package com.washer.Things.domain.machine.entity;

import com.washer.Things.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "machine")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machine_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private MachineType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private Floor floor;

    @Column(length = 10, nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private MachineStatus status = MachineStatus.available;

    @Column(nullable = false)
    private boolean isOutOfOrder = false;

    private LocalDateTime nextAvailableAt;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    public enum MachineType { washing, dryer }
    public enum Floor { _3F, _4F, _5F }
    public enum MachineStatus { available, in_use, reserved }

}
