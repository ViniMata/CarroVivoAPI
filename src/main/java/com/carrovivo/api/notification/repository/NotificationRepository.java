package com.carrovivo.api.notification.repository;

import com.carrovivo.api.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByVehicleId(Long vehicleId);
    List<Notification> findByVehicleIdAndIsReadFalse(Long vehicleId);
}