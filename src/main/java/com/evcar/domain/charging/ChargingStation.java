package com.evcar.domain.charging;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "charging_station")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChargingStation {

    @Id
    @Column(name = "station_id", length = 50, nullable = false)
    private String stationId;

    @Column(name = "station_name", length = 100, nullable = false)
    private String stationName;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "use_time", length = 100)
    private String useTime;

    @Column(name = "zcode", length = 20)
    private String zcode;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "operator_call", length = 20)
    private String operatorCall;

    @Column(name = "parking_free", length = 20)
    private String parkingFree;

    // 🔥 핵심: 비고 컬럼 (NULL 허용)
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // 🔥 충전기 목록 (양방향 관계)
    @OneToMany(mappedBy = "chargingStation", fetch = FetchType.LAZY)
    private List<Charger> chargers;
}