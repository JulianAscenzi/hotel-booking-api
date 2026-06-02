CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    room_id BIGINT NOT NULL REFERENCES rooms(id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price NUMERIC(12, 2) NOT NULL CHECK (total_price > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in)
);

CREATE INDEX idx_bookings_user_created_at ON bookings(user_id, created_at DESC);
CREATE INDEX idx_bookings_room_dates_status ON bookings(room_id, check_in, check_out, status);
