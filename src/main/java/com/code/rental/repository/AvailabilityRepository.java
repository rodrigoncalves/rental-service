package com.code.rental.repository;

import com.code.rental.domain.AvailabilityEntry;
import com.code.rental.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
