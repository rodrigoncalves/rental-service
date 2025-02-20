package com.code.rental.service;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.domain.Booking;
import com.code.rental.domain.Property;
import com.code.rental.domain.enums.BookingStatusEnum;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.BookingRepository;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final JwtService jwtService;

    @Transactional
    public BookingResponseDTO createBooking(final BookingRequestDTO bookingDTO) {
        final Property property = propertyRepository.findById(bookingDTO.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID " + bookingDTO.getPropertyId()));

        if (property.getOwner().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new IllegalArgumentException("You can't book your own property");
        }

        final boolean hasConflictingBookings = bookingRepository.existsByPropertyAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                property, bookingDTO.getEndDate(), bookingDTO.getStartDate(), BookingStatusEnum.ACTIVE);

        if (hasConflictingBookings) {
            throw new IllegalArgumentException("Property is already booked for the selected dates");
        }

        final Booking booking = Booking.builder()
                .property(property)
                .guest(jwtService.getLoggedUser())
                .guestName(bookingDTO.getGuestName())
                .guestEmail(bookingDTO.getGuestEmail())
                .guestPhone(bookingDTO.getGuestPhone())
                .startDate(bookingDTO.getStartDate())
                .endDate(bookingDTO.getEndDate())
                .build();

        final Booking savedBooking = bookingRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    public BookingResponseDTO updateBooking(final Long id, final BookingRequestDTO bookingDTO) {
        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Booking.class, id));

        if (!booking.getGuest().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new IllegalArgumentException("You can't update a booking that you didn't create");
        }

        booking.setGuestName(bookingDTO.getGuestName());
        booking.setGuestEmail(bookingDTO.getGuestEmail());
        booking.setGuestPhone(bookingDTO.getGuestPhone());
        booking.setStartDate(bookingDTO.getStartDate());
        booking.setEndDate(bookingDTO.getEndDate());

        final Booking savedBooking = bookingRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    private BookingResponseDTO mapToDTO(final Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .propertyId(booking.getProperty().getId())
                .guestId(booking.getGuest().getId())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .ownerId(booking.getProperty().getOwner().getId())
                .build();
    }
}
