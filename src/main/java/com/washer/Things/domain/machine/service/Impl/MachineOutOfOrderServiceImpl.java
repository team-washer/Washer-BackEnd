package com.washer.Things.domain.machine.service.Impl;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.presentation.dto.request.MachineOutOfOrderRequest;
import com.washer.Things.domain.machine.presentation.dto.response.MachineOutOfOrderResponse;
import com.washer.Things.domain.machine.repository.MachineRepository;
import com.washer.Things.domain.machine.service.MachineOutOfOrderService;
import com.washer.Things.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineOutOfOrderServiceImpl implements MachineOutOfOrderService {

    private final MachineRepository machineRepository;
    @Transactional
    public List<MachineOutOfOrderResponse> getMachines(Machine.MachineType type, Machine.Floor floor) {
        List<Machine> machines = machineRepository.findByOptionalFilters(type, floor);

        return machines.stream()
                .map(machine -> MachineOutOfOrderResponse.builder()
                        .name(machine.getName())
                        .type(machine.getType())
                        .floor(machine.getFloor())
                        .isOutOfOrder(machine.isOutOfOrder())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMachineStatus(MachineOutOfOrderRequest request) {
        Machine machine = machineRepository.findByName(request.getName())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 이름의 기기를 찾을 수 없습니다."));

        machine.setOutOfOrder(request.isOutOfOrder());
    }
}
