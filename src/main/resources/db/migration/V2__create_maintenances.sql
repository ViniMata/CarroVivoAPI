CREATE TABLE maintenances (
    id            BIGSERIAL PRIMARY KEY,
    vehicle_id    BIGINT        NOT NULL,
    service_type  VARCHAR(200)  NOT NULL,
    description   VARCHAR(500),
    cost          NUMERIC(10,2),
    performed_at  TIMESTAMP,
    next_due_date TIMESTAMP,
    is_recurring  BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_maintenance_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);