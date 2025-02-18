package com.code.rental.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponseDTO {
    private Long userId;
    private String accessToken;
    private final String type = "Bearer";
    private String name;
    private String email;
}
