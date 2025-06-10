package com.washer.Things.domain.machine.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReportMachineErrorRequest {
    private String machineName;
    private String description;
}