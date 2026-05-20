package com.carrovivo.api.diagnostic.model;

import com.carrovivo.api.vehicle.model.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnostics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnostic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String description;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        this.readAt = LocalDateTime.now();
    }
}