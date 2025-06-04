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

    @Column
    private String name;        //Dryer-4F-R1

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private MachineType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private Floor floor;

    @Column(name = "is_out_of_order", columnDefinition = "TINYINT(1)")
    private boolean isOutOfOrder = false;       //고장 여부

    private LocalDateTime nextAvailableAt;      //다음 사용가능 시간

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    public enum MachineType {
        washing, dryer
    }

    public enum Floor {
        _3F, _4F, _5F
    }
}
