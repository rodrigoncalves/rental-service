package com.code.rental.controller.dto.validation;

import com.code.rental.controller.dto.request.BookingRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, BookingRequestDTO> {

    @Override
    public boolean isValid(BookingRequestDTO bookingRequestDTO, ConstraintValidatorContext context) {
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