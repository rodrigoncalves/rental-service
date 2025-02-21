package com.code.rental.controller;

import com.code.rental.controller.dto.request.LoginDTO;
import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.controller.dto.response.LoginResponseDTO;
import com.code.rental.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerUserShouldReturn201() throws Exception {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .name("guest1")
                .email("email@gmail.com")
                .password("123456")
                .build();

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(1L);

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerUserShouldReturn400() throws Exception {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticateUserShouldReturn200() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .email("guest1@gmail.com")
                .password("123456")
                .build();

        when(userService.authenticate(any(LoginDTO.class))).thenReturn(null);

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticateUserShouldReturn400() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .email("invalid-email")
                .password("any-password")
                .build();

        when(userService.authenticate(any(LoginDTO.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }
}