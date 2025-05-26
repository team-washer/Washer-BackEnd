package com.washer.Things.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.washer.Things.domain.room.entity.Room;
import com.washer.Things.domain.user.entity.enums.Gender;
import com.washer.Things.domain.user.entity.enums.Role;
import com.washer.Things.domain.user.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "user")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String name;

    private String schoolNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "email_verify_status", columnDefinition = "TINYINT(1)")
    private boolean emailVerifyStatus;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Convert(converter = StringListConverter.class)
    private List<Role> roles;

    @Column(name = "restricted_until")
    private LocalDateTime restrictedUntil;

    @Column(name = "restriction_reason")
    private String restrictionReason;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "reportedBy", cascade = CascadeType.ALL)
    private List<MachineReport> reports = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}