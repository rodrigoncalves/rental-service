package com.code.rental.repository;

import com.code.rental.domain.Block;
import com.code.rental.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findAllByPropertyId(Long propertyId);

    boolean existsByPropertyAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Property property, LocalDate startDate, LocalDate endDate);
}
