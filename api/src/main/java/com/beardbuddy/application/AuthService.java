package com.beardbuddy.application;

import com.beardbuddy.config.JwtService;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.exception.DomainRuleException;
import com.beardbuddy.domain.exception.NotFoundException;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.CustomerDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Returns a signed token, or throws when the credentials do not match. */
    @Transactional(readOnly = true)
    public String login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new DomainRuleException("Invalid username or password");
        }

        User user = userRepository.findAll().stream()
                .filter(candidate -> username.equals(candidate.getUsername()))
                .findFirst()
                .orElseThrow(() -> new DomainRuleException("Invalid username or password"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new DomainRuleException("Invalid username or password");
        }

        return jwtService.issue(user.getId(), user.getUsername(), user.getRole().name());
    }

    /** The signed-in user, resolved from the JWT subject rather than from a request parameter. */
    @Transactional(readOnly = true)
    public CustomerDto currentUser(String userId) {
        return userRepository.findById(userId)
                .map(CustomerDto::from)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
