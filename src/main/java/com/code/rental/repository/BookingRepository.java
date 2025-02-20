package com.code.rental.repository;

import com.code.rental.domain.Booking;
import com.code.rental.domain.Property;
import com.code.rental.domain.enums.BookingStatusEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByPropertyAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(Property property, LocalDate endDate, LocalDate startDate, BookingStatusEnum bookingStatus);
}
