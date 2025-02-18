package com.code.rental.service;

import com.code.rental.controller.dto.request.LoginDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.controller.dto.response.LoginResponseDTO;
import com.code.rental.domain.User;
import com.code.rental.exception.BadRequestException;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUser(final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, email));
    }

    @Transactional
    public LoginResponseDTO createUser(final UserRequestDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email already exists: " + userDTO.getEmail());
        }

        User user = User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .build();
        userRepository.save(user);

        return authService.authenticate(LoginDTO.builder()
                .email(user.getEmail())
                .password(userDTO.getPassword())
                .build());
    }

    public LoginResponseDTO authenticate(final LoginDTO loginDTO) {
        return authService.authenticate(loginDTO);
    }
}
