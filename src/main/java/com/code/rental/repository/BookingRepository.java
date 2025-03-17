package com.code.rental.repository;

import com.code.rental.domain.Booking;
import com.code.rental.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Booking b WHERE b.property = :property" +
            " AND b.status = 'ACTIVE'" +
            " AND ((b.startDate <= :startDate AND :startDate <= b.endDate)" +
            " OR (b.startDate <= :endDate AND :endDate <= b.endDate)" +
            " OR (:startDate <= b.startDate AND b.endDate <= :endDate))")
    boolean hasActiveBookingConflict(Property property, LocalDate endDate, LocalDate startDate);
}
