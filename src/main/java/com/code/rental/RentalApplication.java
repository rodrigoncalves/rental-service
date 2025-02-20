package com.code.rental;

import com.code.rental.controller.dto.request.UserRequestDTO;
import com.code.rental.domain.Property;
import com.code.rental.repository.PropertyRepository;
import com.code.rental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@SpringBootApplication
public class RentalApplication {

    private final UserService userService;
    private final PropertyRepository propertyRepository;

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }

    @Bean
    CommandLineRunner runner() {
        return args -> {
            if (userService.getAllUsers().isEmpty()) {
                userService.createUser(UserRequestDTO.builder()
                        .name("guest1")
                        .email("guest1@gmail.com")
                        .password("123456")
                        .build());
                userService.createUser(UserRequestDTO.builder()
                        .name("guest2")
                        .email("guest2@gmail.com")
                        .password("123456")
                        .build());
                userService.createUser(UserRequestDTO.builder()
                        .name("owner1")
                        .email("owner1@gmail.com")
                        .password("123456")
                        .build());
                userService.createUser(UserRequestDTO.builder()
                        .name("owner2")
                        .email("owner2@gmail.com")
                        .password("123456")
                        .build());

                propertyRepository.save(Property.builder()
                        .name("Beach House")
                        .description("3 bedroom beach house")
                        .location("Miami Beach")
                        .owner(userService.getUserById(3L))
                        .build());

                // owner2 has 2 properties
                propertyRepository.save(Property.builder()
                        .name("Mountain Cabin")
                        .description("2 bedroom mountain cabin")
                        .location("Aspen")
                        .owner(userService.getUserById(4L))
                        .build());
                propertyRepository.save(Property.builder()
                        .name("City Loft")
                        .description("1 bedroom city loft")
                        .location("New York City")
                        .owner(userService.getUserById(4L))
                        .build());
            }
        };
    }

}
