package com.code.rental.service;

import com.code.rental.controller.dto.request.BlockRequestDTO;
import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.controller.dto.response.BookingResponseDTO;
import com.code.rental.domain.Property;
import com.code.rental.domain.User;
import com.code.rental.domain.enums.BookingStatusEnum;
import com.code.rental.exception.ConflictException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BlockService blockService;

    @MockBean
    private JwtService jwtService;
    private User owner;
    private User guest;

    @BeforeEach
    @Transactional
    public void setUp() {
        userService.createUser(UserRequestDTO.builder()
                .name("Guest")
                .email("guest@gmail.com")
                .password("123456")
                .build());

        userService.createUser(UserRequestDTO.builder()
                .name("Owner")
                .email("owner@gmail.com")
                .password("123456")
                .build());

        guest = userService.getUserById(1L);
        owner = userService.getUserById(2L);

        propertyRepository.save(Property.builder()
                .name("Beach House")
                .description("3 bedroom beach house")
                .location("Miami Beach")
                .owner(owner)
                .build());

        when(jwtService.getLoggedUser()).thenReturn(guest);
    }

    @Test
    void shouldThrowIfNoPropertyWasFound() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(99L)
                .build();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });

        assertThat(ex.getMessage()).isEqualTo("Property not found with ID 99");
    }

    @Test
    void shouldThrowIfPropertyOwnerIsTheSameAsGuest() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .build();

        when(jwtService.getLoggedUser()).thenReturn(owner);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });

        assertThat(ex.getMessage()).isEqualTo("You can't book your own property");
    }

    @Test
    void shouldCreateBooking() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();


        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(bookingDTO);
        assertThat(bookingResponseDTO).isNotNull();
        assertThat(bookingResponseDTO.getId()).isEqualTo(1L);
        assertThat(bookingResponseDTO.getOwnerId()).isEqualTo(owner.getId());
        assertThat(bookingResponseDTO.getGuestId()).isEqualTo(guest.getId());
    }

    @Test
    void shouldCreateBookingWithOverlappingCanceledBooking() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);
        bookingService.cancelBooking(1L);

        final BookingRequestDTO newBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(newBookingDTO);
        assertThat(bookingResponseDTO).isNotNull();
        assertThat(bookingResponseDTO.getId()).isEqualTo(2L);
        assertThat(bookingResponseDTO.getOwnerId()).isEqualTo(owner.getId());
        assertThat(bookingResponseDTO.getGuestId()).isEqualTo(guest.getId());
    }

    @Test
    void shouldThrowIfThereIsBookingConflict() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-05-31"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(BookingRequestDTO.builder()
                    .propertyId(1L)
                    .startDate(LocalDate.parse("2025-06-05"))
                    .endDate(LocalDate.parse("2025-06-15"))
                    .build());
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");

        ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(BookingRequestDTO.builder()
                    .propertyId(1L)
                    .startDate(LocalDate.parse("2025-06-05"))
                    .endDate(LocalDate.parse("2025-06-05"))
                    .build());
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");

        ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(BookingRequestDTO.builder()
                    .propertyId(1L)
                    .startDate(LocalDate.parse("2025-06-01"))
                    .endDate(LocalDate.parse("2025-06-01"))
                    .build());
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");

        ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(BookingRequestDTO.builder()
                    .propertyId(1L)
                    .startDate(LocalDate.parse("2025-06-10"))
                    .endDate(LocalDate.parse("2025-06-10"))
                    .build());
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");

        ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(BookingRequestDTO.builder()
                    .propertyId(1L)
                    .startDate(LocalDate.parse("2025-05-20"))
                    .endDate(LocalDate.parse("2025-06-01"))
                    .build());
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");

        ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(BookingRequestDTO.builder()
                    .propertyId(1L)
                    .startDate(LocalDate.parse("2024-01-01"))
                    .endDate(LocalDate.parse("2025-06-01"))
                    .build());
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldThrowIfThereIsBlockConflict() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO);

        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldThrowIfBlockOverlapsWithBookingStart() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-05"))
                .build();
        blockService.createBlock(blockDTO);

        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-05"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldThrowIfBlockOverlapsWithBookingEnd() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-05"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO);

        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-05"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldThrowIfBlockFullyContainsBooking() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO);

        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-03"))
                .endDate(LocalDate.parse("2025-06-07"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldThrowIfBookingFullyContainsBlock() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-05"))
                .endDate(LocalDate.parse("2025-06-07"))
                .build();
        blockService.createBlock(blockDTO);

        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldNotAllowThrowIfBookingOverlapsWithMultipleBlocks() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO1 = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-05"))
                .build();
        blockService.createBlock(blockDTO1);

        final BlockRequestDTO blockDTO2 = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-06"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO2);

        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-03"))
                .endDate(LocalDate.parse("2025-06-12"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldCancelBooking() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        bookingService.cancelBooking(1L);
        final BookingResponseDTO canceledBooking = bookingService.getBookingById(1L);
        assertThat(canceledBooking.getStatus()).isEqualTo(BookingStatusEnum.CANCELED);
    }

    @Test
    void shouldThrowCancelIfBookingDoesNotExist() {
        final ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.cancelBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("Booking not found with ID 1");
    }

    @Test
    void shouldThrowCancelIfBookingIsNotCreatedByGuest() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        when(jwtService.getLoggedUser()).thenReturn(owner);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.cancelBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("You can't cancel a booking that you didn't create");
    }

    @Test
    void shouldRebook() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        bookingService.cancelBooking(1L);
        bookingService.reactiveBooking(1L);
        final BookingResponseDTO canceledBooking = bookingService.getBookingById(1L);
        assertThat(canceledBooking.getStatus()).isEqualTo(BookingStatusEnum.ACTIVE);
    }

    @Test
    void shouldThrowRebookingIfBookingDoesNotExist() {
        final ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.reactiveBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("Booking not found with ID 1");
    }

    @Test
    void shouldThrowRebookingIfBookingIsNotCreatedByGuest() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        when(jwtService.getLoggedUser()).thenReturn(owner);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.reactiveBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("You can't reactive a booking that you didn't create");
    }

    @Test
    void shouldThrowRebookingIfThereIsBookingConflict() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-05-31"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);
        bookingService.cancelBooking(1L);

        final BookingRequestDTO newBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-05"))
                .endDate(LocalDate.parse("2025-06-15"))
                .build();

        bookingService.createBooking(newBookingDTO);

        when(jwtService.getLoggedUser()).thenReturn(guest);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.reactiveBooking(1L);
        });

        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldThrowRebookingIfBlockConflict() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        when(jwtService.getLoggedUser()).thenReturn(guest);

        bookingService.createBooking(bookingDTO);
        bookingService.cancelBooking(1L);

        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO);

        when(jwtService.getLoggedUser()).thenReturn(guest);
        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.reactiveBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("Property is not available for the selected dates");
    }

    @Test
    void shouldDeleteBooking() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        bookingService.deleteBooking(1L);
        final ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.getBookingById(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("Booking not found with ID 1");
    }

    @Test
    void shouldThrowDeleteIfBookingDoesNotExist() {
        final ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.deleteBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("Booking not found with ID 1");
    }

    @Test
    void shouldThrowDeleteIfBookingIsNotCreatedByGuest() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        when(jwtService.getLoggedUser()).thenReturn(owner);

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.deleteBooking(1L);
        });
        assertThat(ex.getMessage()).isEqualTo("You can't delete a booking that you didn't create");
    }

    @Test
    void shouldUpdateBooking() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .guestName("Guest test")
                .build();

        bookingService.createBooking(bookingDTO);

        final BookingRequestDTO updatedBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-02"))
                .endDate(LocalDate.parse("2025-06-11"))
                .guestName("Guest test updated")
                .build();

        final BookingResponseDTO updatedBooking = bookingService.updateBooking(1L, updatedBookingDTO);
        assertThat(updatedBooking.getStartDate()).isEqualTo(LocalDate.parse("2025-06-02"));
        assertThat(updatedBooking.getEndDate()).isEqualTo(LocalDate.parse("2025-06-11"));
        assertThat(updatedBooking.getGuestName()).isEqualTo("Guest test updated");
    }

    @Test
    void shouldThrowUpdateIfBookingDoesNotExist() {
        final BookingRequestDTO updatedBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-02"))
                .endDate(LocalDate.parse("2025-06-11"))
                .guestName("Guest test updated")
                .build();

        final ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.updateBooking(1L, updatedBookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("Booking not found with ID 1");
    }

    @Test
    void shouldThrowUpdateIfBookingIsNotCreatedByGuest() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .guestName("Guest test")
                .build();

        bookingService.createBooking(bookingDTO);

        when(jwtService.getLoggedUser()).thenReturn(owner);

        final BookingRequestDTO updatedBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-02"))
                .endDate(LocalDate.parse("2025-06-11"))
                .guestName("Guest test updated")
                .build();

        final ConflictException ex = assertThrows(ConflictException.class, () -> {
            bookingService.updateBooking(1L, updatedBookingDTO);
        });
        assertThat(ex.getMessage()).isEqualTo("You can't update a booking that you didn't create");
    }

    @Test
    void shouldCreateBookingEvenWithExistingNonOverlappingBlock() {
        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();
        blockService.createBlock(blockDTO);

        when(jwtService.getLoggedUser()).thenReturn(guest);
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-20"))
                .endDate(LocalDate.parse("2025-06-30"))
                .build();

        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(bookingDTO);
        assertThat(bookingResponseDTO).isNotNull();
        assertThat(bookingResponseDTO.getId()).isEqualTo(2L);
        assertThat(bookingResponseDTO.getOwnerId()).isEqualTo(owner.getId());
        assertThat(bookingResponseDTO.getGuestId()).isEqualTo(guest.getId());
        assertThat(bookingResponseDTO.getPropertyId()).isEqualTo(1L);
    }

    @Test
    void shouldCreateBookingEvenWithExistingNonOverlappingBooking() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        final BookingRequestDTO newBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-20"))
                .endDate(LocalDate.parse("2025-06-30"))
                .build();

        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(newBookingDTO);
        assertThat(bookingResponseDTO).isNotNull();
        assertThat(bookingResponseDTO.getId()).isEqualTo(2L);
        assertThat(bookingResponseDTO.getOwnerId()).isEqualTo(owner.getId());
        assertThat(bookingResponseDTO.getGuestId()).isEqualTo(guest.getId());
        assertThat(bookingResponseDTO.getPropertyId()).isEqualTo(1L);
    }

    @Test
    void shouldCreateBookingEvenWithExistingNonOverlappingBookingAndBlock() {
        final BookingRequestDTO bookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        bookingService.createBooking(bookingDTO);

        when(jwtService.getLoggedUser()).thenReturn(owner);
        final BlockRequestDTO blockDTO = BlockRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-06-20"))
                .endDate(LocalDate.parse("2025-06-30"))
                .build();
        blockService.createBlock(blockDTO);

        when(jwtService.getLoggedUser()).thenReturn(guest);
        final BookingRequestDTO newBookingDTO = BookingRequestDTO.builder()
                .propertyId(1L)
                .startDate(LocalDate.parse("2025-07-20"))
                .endDate(LocalDate.parse("2025-07-30"))
                .build();

        final BookingResponseDTO bookingResponseDTO = bookingService.createBooking(newBookingDTO);
        assertThat(bookingResponseDTO).isNotNull();
        assertThat(bookingResponseDTO.getId()).isEqualTo(3L);
        assertThat(bookingResponseDTO.getOwnerId()).isEqualTo(owner.getId());
        assertThat(bookingResponseDTO.getGuestId()).isEqualTo(guest.getId());
        assertThat(bookingResponseDTO.getPropertyId()).isEqualTo(1L);
    }

}