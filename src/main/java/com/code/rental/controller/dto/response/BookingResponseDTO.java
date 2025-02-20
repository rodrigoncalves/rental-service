package com.code.rental.controller.dto.response;

import com.code.rental.domain.enums.BookingStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {

    private Long id;
    private BookingStatusEnum status;
    private Long propertyId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private LocalDate endDate;

    private Long guestId;
    private Long ownerId;
}
