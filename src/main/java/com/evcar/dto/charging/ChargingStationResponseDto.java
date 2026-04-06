package com.evcar.dto.charging;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStationResponseDto {

    private String stationId;
    private String stationName;
    private String address;
    private double lat;
    private double lng;

    // 🔥 추가
    private String operatorName;
    private String operatorCall;
    private String useTime;
    private String parkingFree;
    private String note;
}