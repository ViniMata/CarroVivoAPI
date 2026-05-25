package com.carrovivo.api.maintenance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @Size(max = 200, message = "Tipo de serviço deve ter no máximo 200 caracteres")
    private String serviceType;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @DecimalMin(value = "0.0", message = "Custo não pode ser negativo")
    private BigDecimal cost;

    private LocalDateTime performedAt;
    private LocalDateTime nextDueDate;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
}
