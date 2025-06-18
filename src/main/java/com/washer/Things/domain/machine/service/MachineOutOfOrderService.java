package com.washer.Things.domain.machine.service;

import com.washer.Things.domain.machine.entity.Machine;
import com.washer.Things.domain.machine.presentation.dto.request.MachineOutOfOrderRequest;
import com.washer.Things.domain.machine.presentation.dto.response.MachineOutOfOrderResponse;

import java.util.List;

public interface MachineOutOfOrderService {
    List<MachineOutOfOrderResponse> getMachines(Machine.MachineType type, Machine.Floor floor);
    void updateMachineStatus(MachineOutOfOrderRequest request);
}
