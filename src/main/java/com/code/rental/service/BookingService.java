package com.code.rental.service;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.domain.AvailabilityEntry;
import com.code.rental.domain.Property;
import com.code.rental.domain.enums.BookingStatusEnum;
import com.code.rental.exception.ConflictException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.AvailabilityRepository;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BookingService {

    private final PropertyRepository propertyRepository;
    private final AvailabilityRepository availabilityRepository;
    private final JwtService jwtService;

    @Transactional
    public BookingResponseDTO createBooking(final BookingRequestDTO bookingDTO) {
        final Property property = propertyRepository.findById(bookingDTO.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID " + bookingDTO.getPropertyId()));

        if (property.getOwner().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new ConflictException("You can't book your own property");
        }

        final boolean hasConflict = availabilityRepository.hasConflict(property,
                bookingDTO.getStartDate(), bookingDTO.getEndDate());
        if (hasConflict) {
            throw new ConflictException("Property is not available for the selected dates");
        }

        // it can throw DataIntegrityViolationException if there is a conflict due a race condition
        availabilityRepository.insertIfNoConflict(
                bookingDTO.getPropertyId(),
                bookingDTO.getStartDate(),
                bookingDTO.getEndDate(),
                jwtService.getLoggedUser().getId(),
                bookingDTO.getGuestName(),
                bookingDTO.getGuestEmail(),
                bookingDTO.getGuestPhone());

        return mapToDTO(availabilityRepository.findSavedBooking(
                bookingDTO.getPropertyId(),
                jwtService.getLoggedUser().getId(),
                bookingDTO.getStartDate(),
                bookingDTO.getEndDate()
        ));
    }

    @Transactional
    public BookingResponseDTO updateBooking(final Long id, final BookingRequestDTO bookingDTO) {
        final AvailabilityEntry booking = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!booking.getGuest().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't update a booking that you didn't create");
        }

        booking.setGuestName(bookingDTO.getGuestName());
        booking.setGuestEmail(bookingDTO.getGuestEmail());
        booking.setGuestPhone(bookingDTO.getGuestPhone());
        booking.setStartDate(bookingDTO.getStartDate());
        booking.setEndDate(bookingDTO.getEndDate());

        final AvailabilityEntry savedBooking = availabilityRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(final Long id) {
        final AvailabilityEntry booking = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        return mapToDTO(booking);
    }

    @Transactional
    public void cancelBooking(final Long id) {
        final AvailabilityEntry booking = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!booking.getGuest().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't cancel a booking that you didn't create");
        }

        booking.setStatus(BookingStatusEnum.CANCELED);
    }

    @Transactional
    public void reactiveBooking(final Long id) {
        final AvailabilityEntry booking = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!booking.getGuest().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new ConflictException("You can't reactive a booking that you didn't create");
        }

        final boolean hasConflict = availabilityRepository.hasConflict(booking.getProperty(),
                booking.getStartDate(), booking.getEndDate());
        if (hasConflict) {
            throw new ConflictException("Property is not available for the selected dates");
        }
        booking.setStatus(BookingStatusEnum.ACTIVE);
    }

    @Transactional
    public void deleteBooking(final Long id) {
        final AvailabilityEntry booking = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!booking.getGuest().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't delete a booking that you didn't create");
        }

        availabilityRepository.delete(booking);
    }

    private BookingResponseDTO mapToDTO(final AvailabilityEntry booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .status(booking.getStatus())
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
