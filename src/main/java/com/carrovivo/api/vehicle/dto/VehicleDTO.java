package com.carrovivo.api.vehicle.dto;

import com.carrovivo.api.vehicle.model.VehicleBrand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// [SEC-65] DTO COM VALIDAÇÃO COMPLETA — PRIMEIRA LINHA DE DEFESA
// Toda entrada do usuário é validada antes de chegar ao service.
// Cobre tipagem, presença, tamanho e formato (padrão de segurança do challenge).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {

    private Long id;

    // [SEC-66] VALIDAÇÃO DE PLACA — TIPAGEM + PRESENÇA + TAMANHO + FORMATO
    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 8, message = "Placa deve ter entre 7 e 8 caracteres")
    // [SEC-67] PATTERN REGEX — WHITELIST DE CARACTERES
    // Aceita apenas letras maiúsculas, números e hífen.
    // Qualquer outro caractere (incluindo SQL injection e XSS) é rejeitado.
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Placa contém caracteres inválidos")
    private String plate;

    // [SEC-68] TAMANHO MÁXIMO EM TODOS OS CAMPOS DE TEXTO
    // Previne buffer overflow e flooding via campos gigantes.
    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String model;

    // [SEC-69] ENUM PARA MARCA — NORMALIZAÇÃO E WHITELIST
    // Aceita apenas valores do enum VehicleBrand (FORD, CHEVROLET...).
    // Impede valores arbitrários e garante consistência dos dados.
    @NotNull(message = "Marca é obrigatória")
    private VehicleBrand brand;

    // [SEC-70] INTERVALO NUMÉRICO — VALIDAÇÃO DE TIPAGEM
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer year;

    @NotBlank(message = "Nome do proprietário é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String ownerName;

    // [SEC-71] VALIDAÇÃO DE EMAIL — FORMATO RFC
    // @Email valida o formato conforme RFC. Combinado com @Size
    // para prevenir endereços absurdamente longos.
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 200, message = "Email deve ter no máximo 200 caracteres")
    private String ownerEmail;

    @Size(max = 50, message = "Cor deve ter no máximo 50 caracteres")
    private String color;
}
