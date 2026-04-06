package com.evcar.domain.charging;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "charger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charger_pk")
    private Long chargerPk;

    @Column(name = "charger_id", length = 50)
    private String chargerId;

    @Column(name = "charger_type", length = 30)
    private String chargerType;

    @Column(name = "power_type", length = 30)
    private String powerType;

    @Column(length = 30)
    private String status;

    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private ChargingStation chargingStation;
}