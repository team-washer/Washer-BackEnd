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
    private String action;      // 수행된 동작 UPDATE DELETE CREATE

    @Column
    private String oldValues;       // 변경 전 값

    @Column
    private String newValues;       // 변경 후 값

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;       // 클라이언트 정보 브라우저, 디바이스

    @Column
    private String resourceType;        // 변경된 자원 타입 entity

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
