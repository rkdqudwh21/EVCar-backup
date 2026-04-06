package com.evcar.repository.vehicle;

import com.evcar.domain.vehicle.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    @Query("SELECT v FROM Vehicle v " +
           "WHERE v.vehicleStatus = 'ACTIVE' " +
           "AND (:brand IS NULL OR v.brand = :brand) " +
           "AND (:vehicleClass IS NULL OR v.vehicleClass = :vehicleClass) " +
           "ORDER BY v.vehicleId ASC")
    List<Vehicle> findByFilter(@Param("brand") String brand,
                               @Param("vehicleClass") String vehicleClass);

    Optional<Vehicle> findByVehicleId(String vehicleId);
}