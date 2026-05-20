package com.carrovivo.api.vehicle.controller;

import com.carrovivo.api.vehicle.dto.VehicleDTO;
import com.carrovivo.api.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Gerenciamento de veículos")
public class VehicleController {

    private final VehicleService service;

    @GetMapping
    @Operation(summary = "Listar todos os veículos")
    public ResponseEntity<List<VehicleDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<VehicleDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/plate/{plate}")
    @Operation(summary = "Buscar veículo por placa")
    public ResponseEntity<VehicleDTO> findByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(service.findByPlate(plate));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo veículo")
    public ResponseEntity<VehicleDTO> create(@Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo")
    public ResponseEntity<VehicleDTO> update(@PathVariable Long id, @Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover veículo")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}