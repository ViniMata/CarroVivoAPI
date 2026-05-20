CREATE TABLE notifications (
                               id          BIGSERIAL PRIMARY KEY,
                               vehicle_id  BIGINT       NOT NULL,
                               title       VARCHAR(200) NOT NULL,
                               message     VARCHAR(500) NOT NULL,
                               is_read     BOOLEAN DEFAULT FALSE,
                               created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_notification_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);