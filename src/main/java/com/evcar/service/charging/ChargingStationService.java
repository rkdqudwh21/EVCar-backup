package com.evcar.service.charging;

import com.evcar.domain.charging.ChargingStation;
import com.evcar.dto.charging.ChargingStationResponseDto;

import java.util.List;
import java.util.Map;

public interface ChargingStationService {

    List<ChargingStation> findByMapBounds(
            double swLat, double neLat,
            double swLng, double neLng
    );

    List<ChargingStation> findByRegion(String sido, String sigungu);

    List<ChargingStationResponseDto> getStationsByRegion(String sido, String sigungu);

    List<ChargingStationResponseDto> getStationsByZcode(String zcode);

    Map<String, List<String>> getAllRegions();
}