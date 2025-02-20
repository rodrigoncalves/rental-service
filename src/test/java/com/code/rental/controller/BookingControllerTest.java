package com.code.rental.controller;

import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.security.jwt.JwtProvider;
import com.code.rental.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private BookingService bookingService;

    private String accessToken;

    @BeforeEach
    public void setUp() {
        accessToken = jwtProvider.createToken("guest1@gmail.com");
    }

    @Test
    void createBookingShouldReturn201() throws Exception {
        when(bookingService.createBooking(any())).thenReturn(new BookingResponseDTO(1L));

        mockMvc.perform(post("/bookings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":1,\"startDate\":\"2026-01-01\",\"endDate\":\"2026-01-02\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getBookingShouldReturn200() throws Exception {
        when(bookingService.getBookingById(anyLong())).thenReturn(new BookingResponseDTO(1L));

        mockMvc.perform(get("/bookings/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookingShouldReturn200() throws Exception {
        mockMvc.perform(put("/bookings/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":1,\"startDate\":\"2026-01-01\",\"endDate\":\"2026-01-02\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelBookingShouldReturn200() throws Exception {
        mockMvc.perform(put("/bookings/1/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void reactiveBookingShouldReturn200() throws Exception {
        mockMvc.perform(put("/bookings/1/rebook")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBookingShouldReturn204() throws Exception {
        mockMvc.perform(delete("/bookings/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}