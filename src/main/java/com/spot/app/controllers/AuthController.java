package com.spot.app.controllers;

import com.spot.app.dtos.LoginRequestDTO;
import com.spot.app.dtos.RegisterRequestDTO;
import com.spot.app.entities.User;
import com.spot.app.repositories.UserRepository;
import com.spot.app.services.SessionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController extends BaseController {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthController(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {
        String username = body.getUsername();
        String password = body.getPassword();

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty() || !optionalUser.get().getPassword().equals(password)) {
            return unauthorized("Invalid credentials");
        }

        User user = optionalUser.get();
        String token = sessionService.createSession(user.getId());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO body) {
        // Null or empty field checks
        if (body.getUsername() == null || body.getUsername().isBlank() ||
                body.getEmail() == null || body.getEmail().isBlank() ||
                body.getPassword() == null || body.getPassword().isBlank()) {
            return badRequest("All fields (username, email, password) are required");
        }

        // Check for duplicate username/email
        if (userRepository.findByUsername(body.getUsername()).isPresent() ||
                userRepository.findByEmail(body.getEmail()).isPresent()) {
            return conflict("Username or email already in use");
        }

        User user = new User();
        user.setUsername(body.getUsername());
        user.setEmail(body.getEmail());
        user.setPassword(body.getPassword()); // TODO: consider hashing this
        userRepository.save(user);

        String token = sessionService.createSession(user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("token", token));
    }

}
