package com.washer.Things.domain.auditLog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "audit_log")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    private String userId;

    @Column
    private String action;

    @Column
    private String oldValues;

    @Column
    private String newValues;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column
    private String resourceType;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
