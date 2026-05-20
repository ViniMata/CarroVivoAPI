package com.carrovivo.api.dealer.controller;

import com.carrovivo.api.dealer.dto.DealerDTO;
import com.carrovivo.api.dealer.service.DealerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
@Tag(name = "Dealers", description = "Concessionárias próximas e preços de serviços")
public class DealerController {

    private final DealerService service;

    @GetMapping("/nearby")
    @Operation(summary = "Buscar concessionárias próximas por cidade")
    public ResponseEntity<List<DealerDTO>> findNearby(@RequestParam String city) {
        return ResponseEntity.ok(service.findNearby(city));
    }

    @GetMapping("/{dealerId}/prices")
    @Operation(summary = "Listar preços de serviços da concessionária")
    public ResponseEntity<List<DealerDTO>> getPrices(@PathVariable Long dealerId) {
        return ResponseEntity.ok(service.getPrices(dealerId));
    }

    @GetMapping("/{dealerId}/schedule")
    @Operation(summary = "Verificar disponibilidade de agendamento")
    public ResponseEntity<List<DealerDTO>> getSchedule(@PathVariable Long dealerId) {
        return ResponseEntity.ok(service.getSchedule(dealerId));
    }
}