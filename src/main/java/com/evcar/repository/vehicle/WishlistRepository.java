package com.evcar.repository.vehicle;

import com.evcar.domain.vehicle.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, String> {

    boolean existsByUserIdAndVehicleId(String userId, String vehicleId);

    Optional<Wishlist> findByUserIdAndVehicleId(String userId, String vehicleId);

    List<Wishlist> findByUserId(String userId);
}