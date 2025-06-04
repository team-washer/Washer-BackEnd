package com.washer.Things.domain.machine.service;

import com.washer.Things.domain.machine.presentation.dto.response.DeviceInfoResponse;

import java.util.List;
import java.util.Map;

public interface MachineInfoService {
    Map<String, List<DeviceInfoResponse>> getMyDevices(String filterType, String filterFloor);
}
