package com.carrovivo.api.diagnostic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @Size(max = 200, message = "Nome da peça deve ter no máximo 200 caracteres")
    private String partName;

    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "GREEN|YELLOW|RED", message = "Status deve ser GREEN, YELLOW ou RED")
    private String status;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    private LocalDateTime readAt;
}
