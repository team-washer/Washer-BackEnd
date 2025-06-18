package com.washer.Things.domain.machine.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MachineOutOfOrderRequest {
    private String name;
    private boolean isOutOfOrder;
}
