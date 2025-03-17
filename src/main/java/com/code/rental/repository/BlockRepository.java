package com.code.rental.repository;

import com.code.rental.domain.Block;
import com.code.rental.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findAllByPropertyId(Long propertyId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Block b WHERE b.property = :property" +
            " AND ((b.startDate <= :startDate AND :startDate <= b.endDate)" +
            " OR (b.startDate <= :endDate AND :endDate <= b.endDate)" +
            " OR (:startDate <= b.startDate AND b.endDate <= :endDate))")
    boolean hasBlockConflict(Property property, LocalDate startDate, LocalDate endDate);
}
