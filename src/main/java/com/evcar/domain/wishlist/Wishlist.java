package com.evcar.domain.wishlist;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "wishlist")
public class Wishlist {

    @Id
    private String wishlistId;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;
}