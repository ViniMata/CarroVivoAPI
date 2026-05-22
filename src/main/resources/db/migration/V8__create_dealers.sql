CREATE TABLE dealers (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    address      VARCHAR(300),
    phone        VARCHAR(20),
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    distance_km  DOUBLE PRECISION,
    service_price DOUBLE PRECISION,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
