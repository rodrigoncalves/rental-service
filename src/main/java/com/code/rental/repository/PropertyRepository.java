package com.code.rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.code.rental.domain.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
