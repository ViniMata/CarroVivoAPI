package com.carrovivo.api.vehicle.dto;

import com.carrovivo.api.vehicle.model.VehicleBrand;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {

    private Long id;

    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 8, message = "Placa deve ter entre 7 e 8 caracteres")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Placa contém caracteres inválidos")
    private String plate;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String model;

    @NotNull(message = "Marca é obrigatória")
    private VehicleBrand brand;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer year;

    @NotBlank(message = "Nome do proprietário é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String ownerName;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 200, message = "Email deve ter no máximo 200 caracteres")
    private String ownerEmail;

    @Size(max = 50, message = "Cor deve ter no máximo 50 caracteres")
    private String color;
}