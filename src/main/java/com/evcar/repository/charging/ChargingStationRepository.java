// src/main/java/com/evcar/repository/charging/ChargingStationRepository.java
package com.evcar.repository.charging;

import com.evcar.domain.charging.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, String> {

    // 기존 검색 (유지)
    List<ChargingStation> findByAddressContaining(String keyword);

    // ✅ 지역코드 + charger까지 같이 조회 (핵심🔥)
    @Query("""
        SELECT cs FROM ChargingStation cs
        LEFT JOIN FETCH cs.chargers
        WHERE cs.zcode = :zcode
    """)
    List<ChargingStation> findByZcodeWithChargers(@Param("zcode") String zcode);
}