CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(30) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('SINGLE', 'DOUBLE', 'SUITE')),
    price_per_night NUMERIC(12, 2) NOT NULL CHECK (price_per_night > 0),
    description VARCHAR(1000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_rooms_active_type ON rooms(active, type);
