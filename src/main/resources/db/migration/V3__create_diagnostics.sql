CREATE TABLE diagnostics (
    id          BIGSERIAL PRIMARY KEY,
    vehicle_id  BIGINT       NOT NULL,
    part_name   VARCHAR(200) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    description VARCHAR(500),
    read_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_diagnostic_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT chk_diagnostic_status CHECK (status IN ('GREEN', 'YELLOW', 'RED'))
);