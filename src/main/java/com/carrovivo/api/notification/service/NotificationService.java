package com.carrovivo.api.notification.service;

import com.carrovivo.api.exception.ResourceNotFoundException;
import com.carrovivo.api.notification.dto.NotificationDTO;
import com.carrovivo.api.notification.model.Notification;
import com.carrovivo.api.notification.repository.NotificationRepository;
import com.carrovivo.api.vehicle.model.Vehicle;
import com.carrovivo.api.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final VehicleRepository vehicleRepository;

    public List<NotificationDTO> findByVehicle(Long vehicleId) {
        return repository.findByVehicleId(vehicleId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public NotificationDTO send(NotificationDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id: " + dto.getVehicleId()));
        return toDTO(repository.save(toEntity(dto, vehicle)));
    }

    public NotificationDTO markAsRead(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada com id: " + id));
        notification.setIsRead(true);
        return toDTO(repository.save(notification));
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .vehicleId(n.getVehicle().getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private Notification toEntity(NotificationDTO dto, Vehicle vehicle) {
        return Notification.builder()
                .vehicle(vehicle)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .build();
    }
}