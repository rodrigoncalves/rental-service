package com.code.rental.repository;

import com.code.rental.domain.AvailabilityEntry;
import com.code.rental.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntry, Long> {

    @Query("""
                SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
                FROM AvailabilityEntry a
                WHERE a.property = :property
                AND a.status = 'ACTIVE'
                AND (
                    (a.startDate <= :startDate AND :startDate <= a.endDate)
                    OR (a.startDate <= :endDate AND :endDate <= a.endDate)
                    OR (:startDate <= a.startDate AND a.endDate <= :endDate)
                )
            """)
    boolean hasConflict(Property property, LocalDate startDate, LocalDate endDate);

    @Query("""
                SELECT a FROM AvailabilityEntry a
                WHERE a.property.id = :propertyId
                AND a.type = 'BLOCK'
            """)
    List<AvailabilityEntry> findAllBlocksByPropertyId(Long propertyId);

    @Modifying
    @Query(value = """
                INSERT INTO availability_entry (version, property_id, type, status, start_date, end_date, guest_id, guest_name, guest_email, guest_phone)
                SELECT 0, :propertyId, 'BOOKING', 'ACTIVE', :startDate, :endDate, :guestId, :guestName, :guestEmail, :guestPhone
                WHERE NOT EXISTS (
                    SELECT 1 FROM availability_entry
                    WHERE property_id = :propertyId
                    AND status = 'ACTIVE'
                    AND (
                        (start_date <= :startDate AND :startDate <= end_date)
                        OR (start_date <= :endDate AND :endDate <= end_date)
                        OR (:startDate <= start_date AND end_date <= :endDate)
                    )
                )
            """, nativeQuery = true)
    int insertIfNoConflict(Long propertyId, LocalDate startDate, LocalDate endDate, Long guestId, String guestName, String guestEmail, String guestPhone);

    @Query("""
                SELECT a FROM AvailabilityEntry a
                WHERE a.property.id = :propertyId
                AND a.guest.id = :guestId
                AND a.startDate = :startDate
                AND a.endDate = :endDate
                AND a.type = 'BOOKING'
                AND a.status = 'ACTIVE'
            """)
    AvailabilityEntry findSavedBooking(Long propertyId, Long guestId, LocalDate startDate, LocalDate endDate);

}
