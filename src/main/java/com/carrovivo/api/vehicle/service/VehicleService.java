package com.carrovivo.api.vehicle.service;

import com.carrovivo.api.exception.ResourceNotFoundException;
import com.carrovivo.api.vehicle.dto.VehicleDTO;
import com.carrovivo.api.vehicle.model.Vehicle;
import com.carrovivo.api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository repository;

    public List<VehicleDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VehicleDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + id));
    }

    public VehicleDTO findByPlate(String plate) {
        return repository.findByPlate(plate)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com placa: " + plate));
    }

    public VehicleDTO save(VehicleDTO dto) {
        Vehicle vehicle = toEntity(dto);
        return toDTO(repository.save(vehicle));
    }

    public VehicleDTO update(Long id, VehicleDTO dto) {
        Vehicle existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + id));
        existing.setPlate(dto.getPlate());
        existing.setModel(dto.getModel());
        existing.setBrand(dto.getBrand());
        existing.setYear(dto.getYear());
        existing.setOwnerName(dto.getOwnerName());
        existing.setOwnerEmail(dto.getOwnerEmail());
        existing.setColor(dto.getColor());
        return toDTO(repository.save(existing));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Veículo não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    private VehicleDTO toDTO(Vehicle v) {
        return VehicleDTO.builder()
                .id(v.getId())
                .plate(v.getPlate())
                .model(v.getModel())
                .brand(v.getBrand())
                .year(v.getYear())
                .ownerName(v.getOwnerName())
                .ownerEmail(v.getOwnerEmail())
                .color(v.getColor())
                .build();
    }

    private Vehicle toEntity(VehicleDTO dto) {
        return Vehicle.builder()
                .plate(dto.getPlate())
                .model(dto.getModel())
                .brand(dto.getBrand())
                .year(dto.getYear())
                .ownerName(dto.getOwnerName())
                .ownerEmail(dto.getOwnerEmail())
                .color(dto.getColor())
                .build();
    }
}