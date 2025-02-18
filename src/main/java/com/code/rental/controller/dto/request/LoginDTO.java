package com.code.rental.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(@NotBlank @Email(message = "Email should be valid") @Size(min = 3, max = 60) String email,
                       @NotBlank @Size(min = 6, max = 40) String password) {
}
