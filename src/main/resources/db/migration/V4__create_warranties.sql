CREATE TABLE warranties (
    id          BIGSERIAL PRIMARY KEY,
    vehicle_id  BIGINT       NOT NULL,
    description VARCHAR(500),
    start_date  TIMESTAMP    NOT NULL,
    end_date    TIMESTAMP    NOT NULL,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_warranty_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);