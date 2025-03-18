package com.code.rental.concurrency;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.domain.AvailabilityEntry;
import com.code.rental.domain.Property;
import com.code.rental.domain.User;
import com.code.rental.domain.enums.BookingStatusEnum;
import com.code.rental.repository.AvailabilityRepository;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.security.jwt.JwtService;
import com.code.rental.service.BookingService;
import com.code.rental.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ConcurrencyTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    private final LocalDate startDate = LocalDate.now().plusDays(1);
    private final LocalDate endDate = LocalDate.now().plusDays(5);
    private Property property;
    private User owner;
    private User guest;

    @BeforeEach
    @Transactional
    public void setUp() {
        userService.createUser(UserRequestDTO.builder()
                .name("Guest1")
                .email("guest1@gmail.com")
                .password("123456")
                .build());

        userService.createUser(UserRequestDTO.builder()
                .name("Guest2")
                .email("guest2@gmail.com")
                .password("123456")
                .build());

        userService.createUser(UserRequestDTO.builder()
                .name("Guest3")
                .email("guest3@gmail.com")
                .password("123456")
                .build());

        userService.createUser(UserRequestDTO.builder()
                .name("Guest4")
                .email("guest4@gmail.com")
                .password("123456")
                .build());

        userService.createUser(UserRequestDTO.builder()
                .name("Owner")
                .email("owner@gmail.com")
                .password("123456")
                .build());

        guest = userService.getUserById(1L);
        owner = userService.getUserById(5L);

        property = propertyRepository.save(Property.builder()
                .name("Beach House")
                .description("3 bedroom beach house")
                .location("Miami Beach")
                .owner(owner)
                .build());
    }

    @Test
    void shouldPreventDoubleBookingUnderConcurrency() {
        List<User> guests = userService.getAllUsers().stream()
                .filter(u -> !u.equals(owner))
                .toList();

        int threadCount = guests.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    when(jwtService.getLoggedUser()).thenReturn(guests.get(finalI));
                    BookingRequestDTO bookingRequestDTO = BookingRequestDTO.builder()
                            .startDate(startDate)
                            .endDate(endDate)
                            .propertyId(property.getId())
                            .build();

                    bookingService.createBooking(bookingRequestDTO);
                } catch (Exception e) {
                    System.out.println("Thread failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // wait for all threads to finish
        }

        assertThat(availabilityRepository.findAll().size()).isEqualTo(1);
    }

    @Transactional(value = Transactional.TxType.NEVER)
    @Test
    void shouldHandleOptimisticLocking() {
        when(jwtService.getLoggedUser()).thenReturn(guest);

        BookingRequestDTO bookingRequestDTO = BookingRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .propertyId(property.getId())
                .build();

        bookingService.createBooking(bookingRequestDTO);

        final AvailabilityEntry booking1 = availabilityRepository.findById(1L).orElseThrow();
        final AvailabilityEntry booking2 = availabilityRepository.findById(1L).orElseThrow();
        assertThat(booking1 == booking2).isFalse();
        assertThat(booking1.getVersion()).isNotNull();

        booking1.setStatus(BookingStatusEnum.CANCELED);
        availabilityRepository.save(booking1);

        assertThatThrownBy(() -> {
            booking2.setGuestName("Other guest");
            availabilityRepository.save(booking2);
        }).isInstanceOf(OptimisticLockingFailureException.class);

        final AvailabilityEntry updatedBooking = availabilityRepository.findById(1L).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatusEnum.CANCELED);
        assertThat(updatedBooking.getVersion()).isEqualTo(booking1.getVersion() + 1);
    }

}