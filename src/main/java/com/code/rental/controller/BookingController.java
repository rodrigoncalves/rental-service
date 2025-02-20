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
@RequestMapping("bookings")
@RestController
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a booking")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody @Valid BookingRequestDTO bookingDTO) {
        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(bookingDTO);

        final URI location = UriComponentsBuilder.fromPath("/bookings/{id}")
                .buildAndExpand(bookingResponseDTO.getId()).toUri();

        return ResponseEntity.created(location).body(bookingResponseDTO);
    }

    @Operation(summary = "Get a booking")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookingResponseDTO getBooking(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @Operation(summary = "Update booking dates and guest details")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BookingResponseDTO updateBooking(@PathVariable Long id, @RequestBody @Valid BookingRequestDTO bookingDTO) {
        return bookingService.updateBooking(id, bookingDTO);
    }

    @Operation(summary = "Cancel a booking")
    @PutMapping(value = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public void cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
    }

    @Operation(summary = "Rebook a canceled booking")
    @PutMapping(value = "/{id}/rebook", produces = MediaType.APPLICATION_JSON_VALUE)
    public void reactiveBooking(@PathVariable Long id) {
        bookingService.reactiveBooking(id);
    }

    @Operation(summary = "Delete a booking from the system")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
