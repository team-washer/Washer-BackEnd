package com.washer.Things.domain.room.entity;

import com.washer.Things.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "room")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "room")
    private List<User> users;
}
