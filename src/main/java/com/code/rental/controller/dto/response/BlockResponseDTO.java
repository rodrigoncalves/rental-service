package com.code.rental.controller.dto.response;

import com.code.rental.controller.dto.validation.ValidDateRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidDateRange
public class BlockResponseDTO {
    private Long id;
    private Long ownerId;
    private Long propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
}
