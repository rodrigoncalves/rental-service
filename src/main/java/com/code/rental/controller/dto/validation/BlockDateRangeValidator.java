package com.code.rental.controller.dto.validation;

import com.code.rental.controller.dto.request.BlockRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class BlockDateRangeValidator implements ConstraintValidator<BlockValidDateRange, BlockRequestDTO> {

    @Override
    public boolean isValid(BlockRequestDTO bookingRequestDTO, ConstraintValidatorContext context) {
        if (bookingRequestDTO.getStartDate() == null || bookingRequestDTO.getEndDate() == null) {
            return true; // Let @NotNull handle null cases
        }

        if (bookingRequestDTO.getStartDate().isBefore(LocalDate.now())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Start date must not be in the past")
                    .addPropertyNode("startDate")
                    .addConstraintViolation();
            return false;
        }

        if (bookingRequestDTO.getStartDate().isAfter(bookingRequestDTO.getEndDate())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Start date must be before or the same day as end date")
                    .addPropertyNode("startDate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}