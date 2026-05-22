CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100),
    action      VARCHAR(100) NOT NULL,
    resource    VARCHAR(100),
    resource_id VARCHAR(100),
    ip_address  VARCHAR(50),
    status      VARCHAR(20),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);