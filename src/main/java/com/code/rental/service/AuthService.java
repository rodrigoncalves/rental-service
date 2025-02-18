package com.code.rental.service;

import com.code.rental.controller.dto.request.LoginDTO;
import com.code.rental.controller.dto.response.LoginResponseDTO;
import com.code.rental.domain.User;
import com.code.rental.exception.ResourceNotFoundException;
import com.code.rental.repository.UserRepository;
import com.code.rental.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public LoginResponseDTO authenticate(final LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.email(),
                        loginDTO.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(User.class, loginDTO.email()));
        return LoginResponseDTO.builder()
                .accessToken(jwtProvider.generateJwtToken(authentication))
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

}
