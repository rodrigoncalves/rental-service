package com.code.rental.domain;

import com.code.rental.domain.enums.BookingStatusEnum;
import com.code.rental.domain.enums.EntryTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_property_dates", columnList = "property_id, startDate, endDate")
})
public class AvailabilityEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryTypeEnum type; // BOOKING or BLOCK

    @ManyToOne
    @JoinColumn(nullable = false, name = "property_id")
    private Property property;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Version // optimistic locking
    private Long version;

    // Fields only for BOOKING (nullable for BLOCK)
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private User guest;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private BookingStatusEnum status = BookingStatusEnum.ACTIVE;

    @Column(length = 255)
    private String guestName;

    @Column(length = 255)
    private String guestEmail;

    @Column(length = 255)
    private String guestPhone;

}
