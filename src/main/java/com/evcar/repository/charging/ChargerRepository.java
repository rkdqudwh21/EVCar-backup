package com.evcar.repository.charging;

import com.evcar.domain.charging.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByChargingStation_StationId(String stationId);
}