package com.code.rental.controller;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
@RestController("bookings")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking for a property")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody @Valid BookingRequestDTO bookingDTO) {
        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(bookingDTO);

        final URI location = UriComponentsBuilder.fromPath("/bookings/{id}")
                .buildAndExpand(bookingResponseDTO.getId()).toUri();

        return ResponseEntity.created(location).body(bookingResponseDTO);
    }

    @Operation(summary = "Update a booking")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BookingResponseDTO updateBooking(@PathVariable Long id, @RequestBody @Valid BookingRequestDTO bookingDTO) {
        return bookingService.updateBooking(id, bookingDTO);
    }
}
