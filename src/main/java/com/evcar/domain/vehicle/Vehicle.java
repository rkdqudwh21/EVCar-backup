package com.evcar.domain.vehicle;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    
    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "brand", length = 20)
    private String brand;

    @Column(name = "model_name", length = 50)
    private String modelName;

    @Column(name = "vehicle_class", length = 30)
    private String vehicleClass;

    @Column(name = "vehicle_status", length = 10)
    private String vehicleStatus;

    @Column(name = "price_basic")
    private Integer priceBasic;

    @Column(name = "price_premium")
    private Integer pricePremium;

    @Column(name = "driving_range")
    private Integer drivingRange;

    @Column(name = "fast_charging_time", length = 30)
    private String fastChargingTime;

    @Column(name = "slow_charging_time", length = 30)
    private String slowChargingTime;

    @Column(name = "charging_method", length = 30)
    private String chargingMethod;

    @Column(name = "battery_capacity", precision = 5, scale = 1)
    private java.math.BigDecimal batteryCapacity;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "catalog_url", length = 255)
    private String catalogUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
  

    // Getters & Setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getVehicleClass() { return vehicleClass; }
    public void setVehicleClass(String vehicleClass) { this.vehicleClass = vehicleClass; }

    public String getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(String vehicleStatus) { this.vehicleStatus = vehicleStatus; }

    public Integer getPriceBasic() { return priceBasic; }
    public void setPriceBasic(Integer priceBasic) { this.priceBasic = priceBasic; }

    public Integer getPricePremium() { return pricePremium; }
    public void setPricePremium(Integer pricePremium) { this.pricePremium = pricePremium; }

    public Integer getDrivingRange() { return drivingRange; }
    public void setDrivingRange(Integer drivingRange) { this.drivingRange = drivingRange; }

    public String getFastChargingTime() { return fastChargingTime; }
    public void setFastChargingTime(String fastChargingTime) { this.fastChargingTime = fastChargingTime; }

    public String getSlowChargingTime() { return slowChargingTime; }
    public void setSlowChargingTime(String slowChargingTime) { this.slowChargingTime = slowChargingTime; }

    public String getChargingMethod() { return chargingMethod; }
    public void setChargingMethod(String chargingMethod) { this.chargingMethod = chargingMethod; }

    public java.math.BigDecimal getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(java.math.BigDecimal batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCatalogUrl() { return catalogUrl; }
    public void setCatalogUrl(String catalogUrl) { this.catalogUrl = catalogUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}