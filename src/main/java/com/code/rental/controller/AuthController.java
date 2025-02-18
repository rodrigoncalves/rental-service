package com.code.rental.controller;

import com.code.rental.controller.dto.request.LoginDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.controller.dto.response.LoginResponseDTO;
import com.code.rental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Validated
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;

    @PostMapping(value = "/auth/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO userDTO) {
        LoginResponseDTO loginDTO = userService.createUser(userDTO);

        URI location = UriComponentsBuilder.fromPath("/users/{id}")
                .buildAndExpand(loginDTO.getUserId()).toUri();

        return ResponseEntity.created(location).body(loginDTO);
    }

    @PostMapping(value = "/auth/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDTO authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.authenticate(loginDTO);
    }
}

