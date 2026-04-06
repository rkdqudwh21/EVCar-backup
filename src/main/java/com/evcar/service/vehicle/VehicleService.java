package com.evcar.service.vehicle;

import java.util.List;
import com.evcar.dto.vehicle.VehicleDetailDto;
import com.evcar.dto.vehicle.VehicleListDto;

public interface VehicleService {
    List<VehicleListDto> getVehicleList(String brand, String vehicleClass);
    VehicleDetailDto getDetail(String vehicleId);
}