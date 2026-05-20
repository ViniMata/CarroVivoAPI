package com.carrovivo.api.maintenance.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceDTO {

    private Long id;

    @NotNull(message = "ID do veículo é obrigatório")
    private Long vehicleId;

    @NotBlank(message = "Tipo de serviço é obrigatório")
    private String serviceType;

    private String description;
    private BigDecimal cost;
    private LocalDateTime performedAt;
    private LocalDateTime nextDueDate;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
}