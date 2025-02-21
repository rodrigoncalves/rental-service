package com.code.rental.service;

import com.code.rental.controller.dto.request.LoginDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.controller.dto.response.LoginResponseDTO;
import com.code.rental.domain.User;
import com.code.rental.exception.BadRequestException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password")
                .build();

        userService = new UserService(authService, userRepository, passwordEncoder);
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void getUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("John Doe");
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getUserByEmail() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail("john.doe@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("John Doe");
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("john.doe@example.com"));
    }

    @Test
    void createUser() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authService.authenticate(any(LoginDTO.class))).thenReturn(new LoginResponseDTO(1L));

        LoginResponseDTO response = userService.createUser(userRequestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void createUser_EmailExists() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createUser(userRequestDTO));
    }

    @Test
    void authenticate() {
        LoginDTO loginDTO = LoginDTO.builder()
                .email("john.doe@example.com")
                .password("password")
                .build();

        when(authService.authenticate(loginDTO)).thenReturn(new LoginResponseDTO(1L));

        LoginResponseDTO response = userService.authenticate(loginDTO);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
    }
}