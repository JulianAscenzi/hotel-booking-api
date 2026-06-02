INSERT INTO users (name, email, password, role)
VALUES (
    'Hotel Admin',
    'admin@hotel.com',
    '$2a$10$UcKF2t/036qJIn5ZZwnzFeW/dneirJaRqa79DtaMCqV21YHmHy7d6',
    'ADMIN'
)
ON CONFLICT (email) DO NOTHING;
