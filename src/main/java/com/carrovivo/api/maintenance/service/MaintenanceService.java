package com.carrovivo.api.maintenance.service;

import com.carrovivo.api.exception.ResourceNotFoundException;
import com.carrovivo.api.maintenance.dto.MaintenanceDTO;
import com.carrovivo.api.maintenance.model.Maintenance;
import com.carrovivo.api.maintenance.repository.MaintenanceRepository;
import com.carrovivo.api.vehicle.model.Vehicle;
import com.carrovivo.api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository repository;
    private final VehicleRepository vehicleRepository;

    public List<MaintenanceDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MaintenanceDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Manutenção não encontrada com id: " + id));
    }

    public List<MaintenanceDTO> findByVehicleId(Long vehicleId) {
        return repository.findByVehicleId(vehicleId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MaintenanceDTO> findRecurring() {
        return repository.findByIsRecurringTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MaintenanceDTO save(MaintenanceDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + dto.getVehicleId()));
        return toDTO(repository.save(toEntity(dto, vehicle)));
    }

    public MaintenanceDTO update(Long id, MaintenanceDTO dto) {
        Maintenance existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manutenção não encontrada com id: " + id));
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + dto.getVehicleId()));
        existing.setVehicle(vehicle);
        existing.setServiceType(dto.getServiceType());
        existing.setDescription(dto.getDescription());
        existing.setCost(dto.getCost());
        existing.setPerformedAt(dto.getPerformedAt());
        existing.setNextDueDate(dto.getNextDueDate());
        existing.setIsRecurring(dto.getIsRecurring());
        return toDTO(repository.save(existing));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Manutenção não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }

    private MaintenanceDTO toDTO(Maintenance m) {
        return MaintenanceDTO.builder()
                .id(m.getId())
                .vehicleId(m.getVehicle().getId())
                .serviceType(m.getServiceType())
                .description(m.getDescription())
                .cost(m.getCost())
                .performedAt(m.getPerformedAt())
                .nextDueDate(m.getNextDueDate())
                .isRecurring(m.getIsRecurring())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private Maintenance toEntity(MaintenanceDTO dto, Vehicle vehicle) {
        return Maintenance.builder()
                .vehicle(vehicle)
                .serviceType(dto.getServiceType())
                .description(dto.getDescription())
                .cost(dto.getCost())
                .performedAt(dto.getPerformedAt())
                .nextDueDate(dto.getNextDueDate())
                .isRecurring(dto.getIsRecurring())
                .build();
    }
}