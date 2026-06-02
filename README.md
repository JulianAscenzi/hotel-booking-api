# Hotel Booking API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

Production-ready Spring Boot REST API for hotel room management and booking workflows. It includes JWT authentication, role-based access control, PostgreSQL migrations with Flyway, Redis-backed caching, Swagger UI, Docker Compose, and automated tests.

## Architecture

```text
Client / Swagger UI
        |
        v
Spring Security JWT Filter
        |
        v
Controllers -> DTO Validation -> Services -> Repositories -> PostgreSQL
                         |             |
                         |             +-> Flyway migrations
                         |
                         +-> Redis cache for room availability
```

## Tech Stack

- Java 21
- Spring Boot 3.3.12
- Spring Security 6 + JWT with jjwt 0.12.6
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Redis 7 + Spring Cache
- SpringDoc OpenAPI 3
- JUnit 5, Mockito, Testcontainers
- Maven
- Docker, Docker Compose
- GitHub Actions CI

## Run Locally

Start the API, PostgreSQL, and Redis:

```bash
docker compose up --build
```

The API will be available at:

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Seeded admin credentials:

```text
email: admin@hotel.com
password: admin123
```

## Environment Variables

| Variable | Default | Description |
|---|---:|---|
| `SPRING_PROFILES_ACTIVE` | `default` | Use `docker` inside Docker Compose |
| `SERVER_PORT` | `8080` | API port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/hoteldb` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `hotel` | PostgreSQL username |
| `DB_PASSWORD` | `hotelpass` locally | PostgreSQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | local development secret | HMAC signing key, use a strong secret in production |
| `JWT_EXPIRATION` | `86400000` | JWT expiration in milliseconds |

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a client user |
| `POST` | `/api/auth/login` | Public | Login and receive a JWT |
| `GET` | `/api/rooms?page=0&size=10&type=DOUBLE` | Authenticated | List active rooms with pagination and optional type filter |
| `GET` | `/api/rooms/available?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD` | Authenticated | List available rooms, cached for 5 minutes |
| `GET` | `/api/rooms/{id}` | Authenticated | Get one active room |
| `POST` | `/api/rooms` | Admin | Create a room |
| `PUT` | `/api/rooms/{id}` | Admin | Update a room |
| `DELETE` | `/api/rooms/{id}` | Admin | Soft delete a room if it has no future bookings |
| `GET` | `/api/bookings` | Authenticated | Admin sees all bookings, clients see their own |
| `GET` | `/api/bookings/{id}` | Authenticated | Get a booking, scoped by role |
| `POST` | `/api/bookings` | Authenticated | Create booking after availability validation |
| `PUT` | `/api/bookings/{id}/cancel` | Authenticated | Cancel a pending booking |

Use the returned JWT as:

```http
Authorization: Bearer <token>
```

## Business Rules

- Passwords are stored with BCrypt.
- JWT authentication is stateless and expires after 24 hours by default.
- Room writes require `ADMIN`; room reads require authentication.
- Booking creation rejects overlapping non-cancelled bookings.
- Booking price is `pricePerNight * numberOfNights`.
- Only `PENDING` bookings can be cancelled.
- Room deletion is a soft delete (`active=false`) and is blocked when future non-cancelled bookings exist.

## Run Tests

Run the full suite:

```bash
mvn test
```

Integration tests use Testcontainers and require Docker. To run only the Mockito service tests:

```bash
mvn -Dtest=RoomServiceTest,BookingServiceTest test
```

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration`:

- `V1__create_users_table.sql`
- `V2__create_rooms_table.sql`
- `V3__create_bookings_table.sql`
- `V4__insert_admin_user.sql`

## CI

GitHub Actions runs on push and pull requests to `main`, sets up Java 21, starts PostgreSQL and Redis service containers, and runs Maven tests.
