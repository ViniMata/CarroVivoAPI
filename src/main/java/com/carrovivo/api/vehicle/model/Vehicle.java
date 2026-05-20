package com.carrovivo.api.vehicle.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String plate;

    @Column(nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VehicleBrand brand;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "owner_name", nullable = false, length = 200)
    private String ownerName;

    @Column(name = "owner_email", nullable = false, length = 200)
    private String ownerEmail;

    @Column(length = 50)
    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}