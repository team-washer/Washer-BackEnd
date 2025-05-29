package com.washer.Things.domain.smartThingsToken.repository;

import com.washer.Things.domain.smartThingsToken.entity.SmartThingsToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmartThingsTokenRepository extends JpaRepository<SmartThingsToken, Long> {
    Optional<SmartThingsToken> findTopByOrderByIdAsc();

}