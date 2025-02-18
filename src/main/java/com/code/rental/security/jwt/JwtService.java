package com.code.rental.security.jwt;

import com.code.rental.domain.User;
import com.code.rental.repository.UserRepository;
import com.code.rental.security.services.UserPrinciple;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@AllArgsConstructor
@Service
public class JwtService {

    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserPrinciple) authentication.getPrincipal()).getEmail();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN, "Invalid token"));
    }

}
