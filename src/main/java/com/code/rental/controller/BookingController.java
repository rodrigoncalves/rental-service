package com.code.rental.controller;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
@RestController("bookings")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking for a property")
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody @Valid BookingRequestDTO bookingDTO) {
        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(bookingDTO);

        final URI location = UriComponentsBuilder.fromPath("/bookings/{id}")
                .buildAndExpand(bookingResponseDTO.getId()).toUri();

        return ResponseEntity.created(location).body(bookingResponseDTO);
    }
}
