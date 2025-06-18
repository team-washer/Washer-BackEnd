package com.washer.Things.domain.machine.presentation.dto.response;

import com.washer.Things.domain.machine.entity.Machine;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MachineOutOfOrderResponse {
    private String name;
    private Machine.MachineType type;
    private Machine.Floor floor;
    private boolean isOutOfOrder;
}
