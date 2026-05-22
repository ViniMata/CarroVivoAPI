CREATE TABLE vehicles (
    id          BIGSERIAL PRIMARY KEY,
    plate       VARCHAR(10)  NOT NULL UNIQUE,
    model       VARCHAR(100) NOT NULL,
    brand       VARCHAR(100) NOT NULL,
    year        INTEGER      NOT NULL,
    owner_name  VARCHAR(200) NOT NULL,
    owner_email VARCHAR(200) NOT NULL,
    color       VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);