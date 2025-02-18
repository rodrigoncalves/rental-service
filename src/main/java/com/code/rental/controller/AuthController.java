package com.code.rental.controller;

import com.code.rental.controller.dto.request.LoginDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.controller.dto.response.LoginResponseDTO;
import com.code.rental.controller.dto.response.UserDTO;
import com.code.rental.controller.mapper.UserMapper;
import com.code.rental.domain.User;
import com.code.rental.service.AuthService;
import com.code.rental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping(value = "/auth/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRequestDTO userDTO) {
        User user = userService.createUser(userDTO);

        URI location = UriComponentsBuilder.fromPath("/users/{id}")
                .buildAndExpand(user.getId()).toUri();

        return ResponseEntity.created(location).body(userMapper.convertToDTO(user));
    }

    @PostMapping(value = "/auth/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDTO authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        return authService.authenticate(loginDTO);
    }
}

