package com.carrovivo.api.diagnostic.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticDTO {

    private Long id;

    @NotNull(message = "ID do veículo é obrigatório")
    private Long vehicleId;

    @NotBlank(message = "Nome da peça é obrigatório")
    private String partName;

    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "GREEN|YELLOW|RED", message = "Status deve ser GREEN, YELLOW ou RED")
    private String status;

    private String description;
    private LocalDateTime readAt;
}