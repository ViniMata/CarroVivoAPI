package com.carrovivo.api.warranty.repository;

import com.carrovivo.api.warranty.model.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {
    Optional<Warranty> findByVehicleIdAndIsActiveTrue(Long vehicleId);
    List<Warranty> findByVehicleId(Long vehicleId);
}