package com.carrovivo.api.diagnostic.service;

import com.carrovivo.api.diagnostic.dto.DiagnosticDTO;
import com.carrovivo.api.diagnostic.model.Diagnostic;
import com.carrovivo.api.diagnostic.repository.DiagnosticRepository;
import com.carrovivo.api.exception.ResourceNotFoundException;
import com.carrovivo.api.vehicle.model.Vehicle;
import com.carrovivo.api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosticService {

    private final DiagnosticRepository repository;
    private final VehicleRepository vehicleRepository;

    public List<DiagnosticDTO> findByVehicleId(Long vehicleId) {
        return repository.findByVehicleId(vehicleId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DiagnosticDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnóstico não encontrado com id: " + id));
    }

    public List<DiagnosticDTO> findAlerts(Long vehicleId) {
        List<Diagnostic> yellow = repository.findByVehicleIdAndStatus(vehicleId, "YELLOW");
        List<Diagnostic> red = repository.findByVehicleIdAndStatus(vehicleId, "RED");
        yellow.addAll(red);
        return yellow.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DiagnosticDTO save(DiagnosticDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + dto.getVehicleId()));
        return toDTO(repository.save(toEntity(dto, vehicle)));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Diagnóstico não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    private DiagnosticDTO toDTO(Diagnostic d) {
        return DiagnosticDTO.builder()
                .id(d.getId())
                .vehicleId(d.getVehicle().getId())
                .partName(d.getPartName())
                .status(d.getStatus())
                .description(d.getDescription())
                .readAt(d.getReadAt())
                .build();
    }

    private Diagnostic toEntity(DiagnosticDTO dto, Vehicle vehicle) {
        return Diagnostic.builder()
                .vehicle(vehicle)
                .partName(dto.getPartName())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .build();
    }
}