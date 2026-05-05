package com.secureaccess.controller;

import com.secureaccess.dto.LoginRequestDTO;
import com.secureaccess.dto.LoginResponseDTO;
import com.secureaccess.dto.UserResponseDTO;
import com.secureaccess.security.JwtTokenProvider;
import com.secureaccess.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController - Authentication işlemleri
 * OOP Prensipleri: Abstraction (iş mantığı Service'de), Encapsulation
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Login Endpoint
     * POST /api/auth/login
     * JWT Token döndürür
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            UserResponseDTO user = userService.login(loginRequest);

            // JWT Token oluştur
            String token = jwtTokenProvider.generateToken(user.getUsername());

            LoginResponseDTO response = new LoginResponseDTO(
                    "Login successful",
                    user,
                    true,
                    token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponseDTO response = new LoginResponseDTO(
                    e.getMessage(),
                    null,
                    false,
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody com.secureaccess.dto.ForgotPasswordRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.resetPassword(request.getUsername(), request.getEmail(), request.getNewPassword());
            response.put("success", true);
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
