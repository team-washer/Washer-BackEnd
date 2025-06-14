package com.washer.Things.domain.machine.entity;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "machine_report")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MachineReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machine_report_id")
    private Long id;

    @Column
    private String machine;

    @Column
    private String reportedByUserName;

    @Column
    private String reportedByUserNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private ReportStatus status = ReportStatus.pending;

    private LocalDateTime resolvedAt;

    public enum ReportStatus {
        pending, in_progress, resolved
    }
}
