package com.studyleague.controller;

import com.studyleague.model.Role;
import com.studyleague.model.User;
import com.studyleague.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public record RegisterRequest(@NotBlank String name, @Email String email, @NotBlank String password,
                                  @NotNull String role) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }
        
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(parseRole(req.role()));
        
        user = userRepository.save(user);

        Map<String, Object> userDto = userDto(user);
        return ResponseEntity.ok(Map.of(
                "token", fakeToken(user),
                "user", userDto
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isEmpty() || !passwordEncoder.matches(req.password(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        
        User user = userOpt.get();
        Map<String, Object> userDto = userDto(user);
        return ResponseEntity.ok(Map.of(
                "token", fakeToken(user),
                "user", userDto
        ));
    }

    private static Map<String, Object> userDto(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("_id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole() == Role.TEACHER ? "teacher" : "student");
        return m;
    }

    private static String fakeToken(User u) {
        return "fake." + u.getId() + ".token";
    }

    private static Role parseRole(String role) {
        if (role == null) return Role.STUDENT;
        return switch (role.toLowerCase()) {
            case "teacher" -> Role.TEACHER;
            case "student" -> Role.STUDENT;
            default -> Role.STUDENT;
        };
    }
}


