package com.carrovivo.api.diagnostic.controller;

import com.carrovivo.api.diagnostic.dto.DiagnosticDTO;
import com.carrovivo.api.diagnostic.service.DiagnosticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/diagnostics")
@RequiredArgsConstructor
@Tag(name = "Diagnostics", description = "Diagnóstico de peças do veículo")
public class DiagnosticController {

    private final DiagnosticService service;

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Diagnóstico atual do veículo")
    public ResponseEntity<List<DiagnosticDTO>> findByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.findByVehicleId(vehicleId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar diagnóstico por ID")
    public ResponseEntity<DiagnosticDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/vehicle/{vehicleId}/alerts")
    @Operation(summary = "Listar alertas ativos do veículo")
    public ResponseEntity<List<DiagnosticDTO>> findAlerts(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.findAlerts(vehicleId));
    }

    @PostMapping
    @Operation(summary = "Registrar leitura de diagnóstico")
    public ResponseEntity<DiagnosticDTO> create(@Valid @RequestBody DiagnosticDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover diagnóstico")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
