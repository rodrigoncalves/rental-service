package com.code.rental.controller.mapper;

import com.code.rental.controller.dto.response.UserDTO;
import com.code.rental.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements AbstractMapper<User, UserDTO> {

    @Override
    public UserDTO convertToDTO(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .build();
    }
}
