package com.carrovivo.api.maintenance.controller;

import com.carrovivo.api.maintenance.dto.MaintenanceDTO;
import com.carrovivo.api.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
@Tag(name = "Maintenances", description = "Gerenciamento de manutenções")
public class MaintenanceController {

    private final MaintenanceService service;

    @GetMapping
    @Operation(summary = "Listar todas as manutenções")
    public ResponseEntity<List<MaintenanceDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar manutenção por ID")
    public ResponseEntity<MaintenanceDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Listar manutenções por veículo")
    public ResponseEntity<List<MaintenanceDTO>> findByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.findByVehicleId(vehicleId));
    }

    @GetMapping("/recurring")
    @Operation(summary = "Listar manutenções recorrentes")
    public ResponseEntity<List<MaintenanceDTO>> findRecurring() {
        return ResponseEntity.ok(service.findRecurring());
    }

    @PostMapping
    @Operation(summary = "Registrar nova manutenção")
    public ResponseEntity<MaintenanceDTO> create(@Valid @RequestBody MaintenanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar manutenção")
    public ResponseEntity<MaintenanceDTO> update(@PathVariable Long id, @Valid @RequestBody MaintenanceDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover manutenção")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}