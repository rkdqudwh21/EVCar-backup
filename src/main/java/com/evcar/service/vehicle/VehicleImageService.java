package com.evcar.service.vehicle;

import com.evcar.dto.vehicle.VehicleImageResponseDto;
import java.util.List;

public interface VehicleImageService {
    List<VehicleImageResponseDto> getImages(String vehicleId);
}