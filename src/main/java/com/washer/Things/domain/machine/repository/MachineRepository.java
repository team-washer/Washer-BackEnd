package com.washer.Things.domain.machine.repository;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.entity.Machine.MachineType;
import com.washer.Things.domain.machine.entity.Machine.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    @Query("SELECT m FROM Machine m WHERE m.type = :type AND m.floor = :floor AND m.name = :label")
    Optional<Machine> findByTypeAndFloorAndLabel(@Param("type") MachineType type,
                                                 @Param("floor") Floor floor,
                                                 @Param("label") String label);

    Optional<Machine> findByName(String machineName);
}
