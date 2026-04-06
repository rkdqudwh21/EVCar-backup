package com.evcar.service.vehicle;

import java.util.List;
import com.evcar.dto.vehicle.VehicleListDto;

public interface WishlistService {
    boolean isWished(String userId, String vehicleId);
    void addWishlist(String userId, String vehicleId);
    void removeWishlist(String userId, String vehicleId);
    List<VehicleListDto> getWishlistVehicles(String userId);
}