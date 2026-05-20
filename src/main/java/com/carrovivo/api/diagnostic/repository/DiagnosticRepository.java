package com.carrovivo.api.diagnostic.repository;

import com.carrovivo.api.diagnostic.model.Diagnostic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosticRepository extends JpaRepository<Diagnostic, Long> {
    List<Diagnostic> findByVehicleId(Long vehicleId);
    List<Diagnostic> findByVehicleIdAndStatus(Long vehicleId, String status);
}