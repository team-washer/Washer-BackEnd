package com.washer.Things.domain.machine.repository;

import com.washer.Things.domain.machine.entity.MachineReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineReportRepository extends JpaRepository<MachineReport, Long> {
    List<MachineReport> findAllByOrderByIdDesc();
}
