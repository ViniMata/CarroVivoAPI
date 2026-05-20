package com.carrovivo.api.warranty.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyDTO {

    private Long id;

    @NotNull(message = "ID do veículo é obrigatório")
    private Long vehicleId;

    private String description;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime startDate;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDateTime endDate;

    private Boolean isActive;
    private LocalDateTime createdAt;
}