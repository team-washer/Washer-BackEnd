package com.washer.Things.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.washer.Things.domain.user.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
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

    private String room;

    private String schoolNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Convert(converter = StringListConverter.class)
    private List<Role> roles;

}