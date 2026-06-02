package com.portfolio.hotelbooking.controller;

import com.portfolio.hotelbooking.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoomControllerTest extends AbstractIntegrationTest {

    @Test
    void adminCanCreateUpdateAndDeleteRoom() throws Exception {
        String adminToken = adminToken();
        long roomId = createRoom(adminToken, "201");

        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "number": "202",
                                  "type": "SUITE",
                                  "pricePerNight": 300.00,
                                  "description": "Updated suite",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("202"))
                .andExpect(jsonPath("$.type").value("SUITE"));

        mockMvc.perform(delete("/api/rooms/{id}", roomId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void clientCanReadRoomsButCannotCreateThem() throws Exception {
        String adminToken = adminToken();
        createRoom(adminToken, "301");
        String clientToken = registerClientAndGetToken("reader@example.com");

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + clientToken)
                        .param("type", "DOUBLE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].number").value("301"));

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "number": "302",
                                  "type": "DOUBLE",
                                  "pricePerNight": 150.00,
                                  "description": "Forbidden room",
                                  "active": true
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void availableRoomsEndpointReturnsActiveRooms() throws Exception {
        String adminToken = adminToken();
        createRoom(adminToken, "401");
        String clientToken = registerClientAndGetToken("available@example.com");
        LocalDate checkIn = LocalDate.now().plusDays(15);
        LocalDate checkOut = checkIn.plusDays(2);

        mockMvc.perform(get("/api/rooms/available")
                        .header("Authorization", "Bearer " + clientToken)
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value("401"));
    }
}
