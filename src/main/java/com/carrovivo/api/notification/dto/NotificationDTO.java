package com.carrovivo.api.notification.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;

    @NotNull(message = "ID do veículo é obrigatório")
    private Long vehicleId;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Mensagem é obrigatória")
    private String message;

    private Boolean isRead;
    private LocalDateTime createdAt;
}