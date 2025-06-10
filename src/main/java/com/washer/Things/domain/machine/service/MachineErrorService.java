package com.washer.Things.domain.machine.service;

import com.washer.Things.domain.machine.presentation.dto.request.ReportMachineErrorRequest;

public interface MachineErrorService {
    void reportMachineError(ReportMachineErrorRequest request);
}
