package com.code.rental.service;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.domain.Booking;
import com.code.rental.domain.Property;
import com.code.rental.domain.enums.BookingStatusEnum;
import com.code.rental.exception.ConflictException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.BlockRepository;
import com.code.rental.repository.BookingRepository;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final BlockRepository blockRepository;
    private final JwtService jwtService;

    @Transactional
    public BookingResponseDTO createBooking(final BookingRequestDTO bookingDTO) {
        final Property property = propertyRepository.findById(bookingDTO.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID " + bookingDTO.getPropertyId()));

        if (property.getOwner().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new ConflictException("You can't book your own property");
        }

        checkForConflictsBookingsAndBlocks(property, bookingDTO.getStartDate(), bookingDTO.getEndDate());

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

    @Transactional
    public BookingResponseDTO updateBooking(final Long id, final BookingRequestDTO bookingDTO) {
        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Booking.class, id));

        if (!booking.getGuest().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't update a booking that you didn't create");
        }

        booking.setGuestName(bookingDTO.getGuestName());
        booking.setGuestEmail(bookingDTO.getGuestEmail());
        booking.setGuestPhone(bookingDTO.getGuestPhone());
        booking.setStartDate(bookingDTO.getStartDate());
        booking.setEndDate(bookingDTO.getEndDate());

        final Booking savedBooking = bookingRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(final Long id) {
        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Booking.class, id));

        return mapToDTO(booking);
    }

    @Transactional
    public void cancelBooking(final Long id) {
        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Booking.class, id));

        if (!booking.getGuest().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't cancel a booking that you didn't create");
        }

        booking.setStatus(BookingStatusEnum.CANCELED);
    }

    @Transactional
    public void reactiveBooking(final Long id) {
        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Booking.class, id));

        if (!booking.getGuest().getId().equals(jwtService.getLoggedUser().getId())) {
            throw new ConflictException("You can't reactive a booking that you didn't create");
        }

        checkForConflictsBookingsAndBlocks(booking.getProperty(), booking.getStartDate(), booking.getEndDate());

        booking.setStatus(BookingStatusEnum.ACTIVE);
    }

    @Transactional
    public void deleteBooking(final Long id) {
        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Booking.class, id));

        if (!booking.getGuest().equals(jwtService.getLoggedUser())) {
            throw new ConflictException("You can't delete a booking that you didn't create");
        }

        bookingRepository.delete(booking);
    }

    private void checkForConflictsBookingsAndBlocks(final Property property, final LocalDate startDate, final LocalDate endDate) {
        final boolean hasConflictingBookings = bookingRepository.hasActiveBookingConflict(property, startDate, endDate);

        if (hasConflictingBookings) {
            throw new ConflictException("Property is already booked for the selected dates");
        }

        final boolean hasConflictingBlocks = blockRepository.hasBlockConflict(property, startDate, endDate);

        if (hasConflictingBlocks) {
            throw new ConflictException("Property is blocked for the selected dates");
        }
    }

    private BookingResponseDTO mapToDTO(final Booking booking) {
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
