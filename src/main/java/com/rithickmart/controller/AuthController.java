package com.rithickmart.controller;
import com.rithickmart.model.*;
import com.rithickmart.repo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final LoginHistoryRepository history;
    private final PasswordEncoder encoder;
    private final AuthenticationManager auth;

    public AuthController(
            UserRepository users,
            LoginHistoryRepository history,
            PasswordEncoder encoder,
            AuthenticationManager auth) {
        this.users = users;
        this.history = history;
        this.encoder = encoder;
        this.auth = auth;
    }

    record Register(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password,
            String role) {
    }

    record Login(
            @Email @NotBlank String email,
            @NotBlank String password) {
    }

    record UserView(Long id, String name, String email, String role) {
    }

    private UserView view(User user) {
        return new UserView(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody Register r) {

        String email = r.email().trim().toLowerCase();

        if (users.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already registered"));
        }

        User user = new User();
        user.setName(r.name().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(r.password()));
        user.setRole(Role.BUYER);

        return ResponseEntity.ok(view(users.save(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody Login l,
            HttpServletRequest request) {

        String email = l.email().trim().toLowerCase();

        try {
            Authentication authentication = auth.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, l.password()));

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository
                            .SPRING_SECURITY_CONTEXT_KEY,
                    context);

            User user = users.findByEmail(email).orElseThrow();

            history.save(new LoginHistory(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    "LOGIN",
                    request.getRemoteAddr()));

            return ResponseEntity.ok(view(user));

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();

            history.save(new LoginHistory(
                    null,
                    email,
                    "UNKNOWN",
                    "FAILED_LOGIN",
                    request.getRemoteAddr()));

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        return users.findByEmail(authentication.getName())
                .<ResponseEntity<?>>map(
                        user -> ResponseEntity.ok(view(user)))
                .orElseGet(
                        () -> ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            Authentication authentication,
            HttpServletRequest request) {

        if (authentication != null
                && authentication.isAuthenticated()) {

            users.findByEmail(authentication.getName())
                    .ifPresent(user -> history.save(
                            new LoginHistory(
                                    user.getId(),
                                    user.getEmail(),
                                    user.getRole().name(),
                                    "LOGOUT",
                                    request.getRemoteAddr())));
        }

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return Map.of("message", "Logged out");
    }
}