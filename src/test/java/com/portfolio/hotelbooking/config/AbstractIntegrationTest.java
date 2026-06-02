package com.portfolio.hotelbooking.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.hotelbooking.dto.LoginRequest;
import com.portfolio.hotelbooking.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hoteldb")
            .withUsername("hotel")
            .withPassword("hotelpass");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.cache.type", () -> "simple");
        registry.add("jwt.secret", () -> "integration-test-secret-key-for-hotel-booking-api-123456789");
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM bookings");
        jdbcTemplate.execute("DELETE FROM rooms");
        jdbcTemplate.execute("DELETE FROM users WHERE email <> 'admin@hotel.com'");
    }

    protected String adminToken() throws Exception {
        return login("admin@hotel.com", "admin123");
    }

    protected String registerClientAndGetToken(String email) throws Exception {
        String payload = objectMapper.writeValueAsString(new RegisterRequest("Client User", email, "client123"));
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    protected String login(String email, String password) throws Exception {
        String payload = objectMapper.writeValueAsString(new LoginRequest(email, password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    protected long createRoom(String token, String number) throws Exception {
        String payload = """
                {
                  "number": "%s",
                  "type": "DOUBLE",
                  "pricePerNight": 150.00,
                  "description": "Comfortable double room",
                  "active": true
                }
                """.formatted(number);
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}
