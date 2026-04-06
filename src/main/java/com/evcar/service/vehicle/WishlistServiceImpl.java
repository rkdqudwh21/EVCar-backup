package com.evcar.service.vehicle;

import com.evcar.domain.vehicle.Wishlist;
import com.evcar.dto.vehicle.VehicleListDto;
import com.evcar.repository.vehicle.VehicleRepository;
import com.evcar.repository.vehicle.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public boolean isWished(String userId, String vehicleId) {
        return wishlistRepository.existsByUserIdAndVehicleId(userId, vehicleId);
    }

    @Override
    public void addWishlist(String userId, String vehicleId) {
        if (wishlistRepository.existsByUserIdAndVehicleId(userId, vehicleId)) {
            return;
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistId(UUID.randomUUID().toString());
        wishlist.setUserId(userId);
        wishlist.setVehicleId(vehicleId);
        wishlist.setCreatedAt(LocalDateTime.now());

        wishlistRepository.save(wishlist);
    }

    @Override
    public void removeWishlist(String userId, String vehicleId) {
        wishlistRepository.findByUserIdAndVehicleId(userId, vehicleId)
                .ifPresent(wishlistRepository::delete);
    }

    @Override
    public List<VehicleListDto> getWishlistVehicles(String userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        List<VehicleListDto> result = new ArrayList<>();

        for (Wishlist wishlist : wishlists) {
            vehicleRepository.findById(wishlist.getVehicleId()).ifPresent(vehicle -> {
                VehicleListDto dto = new VehicleListDto(
                        vehicle.getVehicleId(),
                        vehicle.getBrand(),
                        vehicle.getModelName(),
                        vehicle.getVehicleClass(),
                        vehicle.getPriceBasic(),
                        vehicle.getPricePremium(),
                        vehicle.getDrivingRange(),
                        vehicle.getImageUrl(),
                        vehicle.getCatalogUrl()
                );
                dto.setWished(true);
                result.add(dto);
            });
        }

        return result;
    }
}