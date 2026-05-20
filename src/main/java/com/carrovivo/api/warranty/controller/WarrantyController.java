package com.carrovivo.api.warranty.controller;

import com.carrovivo.api.warranty.dto.WarrantyDTO;
import com.carrovivo.api.warranty.service.WarrantyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warranties")
@RequiredArgsConstructor
@Tag(name = "Warranties", description = "Gerenciamento de garantias")
public class WarrantyController {

    private final WarrantyService service;

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Buscar garantia ativa do veículo")
    public ResponseEntity<WarrantyDTO> findByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.findByVehicle(vehicleId));
    }

    @GetMapping("/{id}/valid")
    @Operation(summary = "Verificar se garantia está válida")
    public ResponseEntity<Boolean> isValid(@PathVariable Long id) {
        return ResponseEntity.ok(service.isValid(id));
    }

    @PostMapping
    @Operation(summary = "Registrar garantia")
    public ResponseEntity<WarrantyDTO> create(@Valid @RequestBody WarrantyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar garantia")
    public ResponseEntity<WarrantyDTO> update(@PathVariable Long id, @Valid @RequestBody WarrantyDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}