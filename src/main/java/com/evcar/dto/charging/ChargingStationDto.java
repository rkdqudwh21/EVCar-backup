package com.evcar.dto.charging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStationDto {
    private Long stationId;
    private String stationName;
    private double latitude;
    private double longitude;
    private String address;
    private String sido;      // ← 추가
    private String sigungu;   // ← 추가
}
