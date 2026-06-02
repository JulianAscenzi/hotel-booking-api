package com.portfolio.hotelbooking.controller;

import com.portfolio.hotelbooking.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerTest extends AbstractIntegrationTest {

    @Test
    void clientCanCreateAndCancelPendingBooking() throws Exception {
        String adminToken = adminToken();
        long roomId = createRoom(adminToken, "501");
        String clientToken = registerClientAndGetToken("booker@example.com");
        LocalDate checkIn = LocalDate.now().plusDays(20);
        LocalDate checkOut = checkIn.plusDays(3);

        String response = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "roomId": %d,
                                  "checkIn": "%s",
                                  "checkOut": "%s"
                                }
                                """.formatted(roomId, checkIn, checkOut)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(450.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long bookingId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/bookings/{id}/cancel", bookingId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void overlappingBookingIsRejected() throws Exception {
        String adminToken = adminToken();
        long roomId = createRoom(adminToken, "601");
        String firstClientToken = registerClientAndGetToken("first@example.com");
        String secondClientToken = registerClientAndGetToken("second@example.com");
        LocalDate checkIn = LocalDate.now().plusDays(25);
        LocalDate checkOut = checkIn.plusDays(2);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + firstClientToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "roomId": %d,
                                  "checkIn": "%s",
                                  "checkOut": "%s"
                                }
                                """.formatted(roomId, checkIn, checkOut)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + secondClientToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "roomId": %d,
                                  "checkIn": "%s",
                                  "checkOut": "%s"
                                }
                                """.formatted(roomId, checkIn.plusDays(1), checkOut.plusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Room is not available for the requested dates"));
    }

    @Test
    void adminSeesAllBookings() throws Exception {
        String adminToken = adminToken();
        long roomId = createRoom(adminToken, "701");
        String clientToken = registerClientAndGetToken("visible@example.com");
        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(1);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "roomId": %d,
                                  "checkIn": "%s",
                                  "checkOut": "%s"
                                }
                                """.formatted(roomId, checkIn, checkOut)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].room.number").value("701"));
    }
}
