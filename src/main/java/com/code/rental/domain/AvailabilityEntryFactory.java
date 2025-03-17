package com.code.rental.domain;

import com.code.rental.domain.enums.EntryTypeEnum;

import java.time.LocalDate;

public class AvailabilityEntryFactory {

    public static AvailabilityEntry createBooking(
            final Property property,
            final User guest,
            final String guestName,
            final String guestEmail,
            final String guestPhone,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        return AvailabilityEntry.builder()
                .type(EntryTypeEnum.BOOKING)
                .property(property)
                .guest(guest)
                .guestName(guestName)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public static AvailabilityEntry createBlock(Property property, LocalDate startDate, LocalDate endDate) {
        return AvailabilityEntry.builder()
                .type(EntryTypeEnum.BLOCK)
                .property(property)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
