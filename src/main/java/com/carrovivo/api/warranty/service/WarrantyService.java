package com.carrovivo.api.warranty.service;

import com.carrovivo.api.exception.ResourceNotFoundException;
import com.carrovivo.api.vehicle.model.Vehicle;
import com.carrovivo.api.vehicle.repository.VehicleRepository;
import com.carrovivo.api.warranty.dto.WarrantyDTO;
import com.carrovivo.api.warranty.model.Warranty;
import com.carrovivo.api.warranty.repository.WarrantyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WarrantyService {

    private final WarrantyRepository repository;
    private final VehicleRepository vehicleRepository;

    public WarrantyDTO findByVehicle(Long vehicleId) {
        return repository.findByVehicleIdAndIsActiveTrue(vehicleId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Garantia não encontrada para o veículo: " + vehicleId));
    }

    public WarrantyDTO save(WarrantyDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + dto.getVehicleId()));
        return toDTO(repository.save(toEntity(dto, vehicle)));
    }

    public WarrantyDTO update(Long id, WarrantyDTO dto) {
        Warranty existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Garantia não encontrada com id: " + id));
        existing.setDescription(dto.getDescription());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setIsActive(dto.getIsActive());
        return toDTO(repository.save(existing));
    }

    public boolean isValid(Long id) {
        Warranty warranty = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Garantia não encontrada com id: " + id));
        return warranty.getIsActive() && warranty.getEndDate().isAfter(LocalDateTime.now());
    }

    private WarrantyDTO toDTO(Warranty w) {
        return WarrantyDTO.builder()
                .id(w.getId())
                .vehicleId(w.getVehicle().getId())
                .description(w.getDescription())
                .startDate(w.getStartDate())
                .endDate(w.getEndDate())
                .isActive(w.getIsActive())
                .createdAt(w.getCreatedAt())
                .build();
    }

    private Warranty toEntity(WarrantyDTO dto, Vehicle vehicle) {
        return Warranty.builder()
                .vehicle(vehicle)
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }
}