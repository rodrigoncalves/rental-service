package com.code.rental.repository;

import com.code.rental.domain.Booking;
import com.code.rental.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Booking b WHERE b.property = ?1 AND b.endDate >= ?2 AND b.startDate <= ?3 AND b.status = 'ACTIVE'")
    boolean hasActiveBookingConflict(Property property, LocalDate endDate, LocalDate startDate);
}
