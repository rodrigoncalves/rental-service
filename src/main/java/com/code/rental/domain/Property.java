package com.code.rental.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table
@Entity
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String description;
    @Size(max = 255)
    private String location;

    @ManyToOne
    @JoinColumn(nullable = false, name = "owner_id")
    private User owner;
}
