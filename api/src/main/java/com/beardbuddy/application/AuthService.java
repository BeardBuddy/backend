package com.beardbuddy.application;

import com.beardbuddy.config.JwtService;
import com.beardbuddy.domain.User;
import com.beardbuddy.domain.exception.DomainRuleException;
import com.beardbuddy.domain.exception.NotFoundException;
import com.beardbuddy.repository.UserRepository;
import com.beardbuddy.web.dto.CustomerDto;
import com.beardbuddy.web.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("[a-z0-9._-]{3,32}");
    private static final Pattern PHONE = Pattern.compile("\\+?[0-9 ()-]{6,20}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final int MIN_PASSWORD = 8;
    // BCrypt only looks at the first 72 bytes, and Spring rejects anything longer.
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final int MAX_NAME = 50;

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

        User user = userRepository.findByUsername(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new DomainRuleException("Invalid username or password"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new DomainRuleException("Invalid username or password");
        }

        return issueToken(user);
    }

    /** Creates a customer account and returns a token, so the new user is signed in right away. */
    @Transactional
    public String register(RegisterRequest request) {
        if (request == null) {
            throw new DomainRuleException("Request body is required");
        }

        String username = required(request.username(), "Username").toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(username).matches()) {
            throw new DomainRuleException(
                    "Username must be 3-32 characters: letters, digits, dot, dash or underscore");
        }

        String password = request.password();
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new DomainRuleException("Password must be at least " + MIN_PASSWORD + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new DomainRuleException("Password is too long (max " + MAX_PASSWORD_BYTES + " bytes)");
        }

        String firstName = name(request.firstName(), "First name");
        String lastName = name(request.lastName(), "Last name");

        String phone = required(request.phone(), "Phone");
        if (!PHONE.matcher(phone).matches()) {
            throw new DomainRuleException("Phone number looks invalid");
        }

        String email = request.email() == null || request.email().isBlank() ? null : request.email().trim();
        if (email != null && (email.length() > 254 || !EMAIL.matcher(email).matches())) {
            throw new DomainRuleException("Email looks invalid");
        }

        if (userRepository.existsByUsername(username)) {
            throw new DomainRuleException("Username is already taken");
        }

        User user = User.customer(
                UUID.randomUUID().toString(),
                firstName,
                lastName,
                phone,
                email,
                username,
                passwordEncoder.encode(password)
        );
        userRepository.saveAndFlush(user);

        return issueToken(user);
    }

    /** The signed-in user, resolved from the JWT subject rather than from a request parameter. */
    @Transactional(readOnly = true)
    public CustomerDto currentUser(String userId) {
        return userRepository.findById(userId)
                .map(CustomerDto::from)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String issueToken(User user) {
        return jwtService.issue(user.getId(), user.getUsername(), user.getRole().name());
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleException(field + " is required");
        }
        return value.trim();
    }

    private static String name(String value, String field) {
        String trimmed = required(value, field);
        if (trimmed.length() > MAX_NAME) {
            throw new DomainRuleException(field + " is too long (max " + MAX_NAME + " characters)");
        }
        return trimmed;
    }
}
