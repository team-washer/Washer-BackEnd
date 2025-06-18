package com.washer.Things.domain.machine.repository;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.entity.Machine.MachineType;
import com.washer.Things.domain.machine.entity.Machine.Floor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    @Query("SELECT m FROM Machine m WHERE m.type = :type AND m.floor = :floor AND m.name = :label")
    Optional<Machine> findByTypeAndFloorAndLabel(@Param("type") MachineType type,
                                                 @Param("floor") Floor floor,
                                                 @Param("label") String label);

    Optional<Machine> findByName(String machineName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Machine m WHERE m.id = :machineId")
    Optional<Machine> findWithLockById(@Param("machineId") Long machineId);

    @Query("SELECT m FROM Machine m " +
            "WHERE (:type IS NULL OR m.type = :type) " +
            "AND (:floor IS NULL OR m.floor = :floor)")
    List<Machine> findByOptionalFilters(@Param("type") Machine.MachineType type,
                                        @Param("floor") Machine.Floor floor);
}
