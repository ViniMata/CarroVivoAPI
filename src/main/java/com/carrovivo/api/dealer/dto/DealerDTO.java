package com.carrovivo.api.dealer.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class DealerDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String name;

    @Size(max = 300, message = "Endereço deve ter no máximo 300 caracteres")
    private String address;

    @Pattern(regexp = "^[0-9()\\- +]*$", message = "Telefone contém caracteres inválidos")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @DecimalMin(value = "-90.0", message = "Latitude inválida")
    @DecimalMax(value = "90.0", message = "Latitude inválida")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude inválida")
    @DecimalMax(value = "180.0", message = "Longitude inválida")
    private Double longitude;

    @DecimalMin(value = "0.0", message = "Distância não pode ser negativa")
    private Double distanceKm;

    @DecimalMin(value = "0.0", message = "Preço não pode ser negativo")
    private Double servicePrice;

    private LocalDateTime createdAt;
}
