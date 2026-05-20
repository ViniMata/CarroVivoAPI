package com.carrovivo.api.notification.controller;

import com.carrovivo.api.notification.dto.NotificationDTO;
import com.carrovivo.api.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notificações e alertas do veículo")
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Listar notificações do veículo")
    public ResponseEntity<List<NotificationDTO>> findByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.findByVehicle(vehicleId));
    }

    @PostMapping("/send")
    @Operation(summary = "Enviar notificação")
    public ResponseEntity<NotificationDTO> send(@Valid @RequestBody NotificationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.send(dto));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markAsRead(id));
    }
}