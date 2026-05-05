package com.secureaccess.controller;

import com.secureaccess.dto.LoginRequestDTO;
import com.secureaccess.dto.LoginResponseDTO;
import com.secureaccess.dto.UserResponseDTO;
import com.secureaccess.entity.AccessStatus;
import com.secureaccess.security.JwtTokenProvider;
import com.secureaccess.service.AccessLogService;
import com.secureaccess.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AccessLogService accessLogService;
    private final HttpServletRequest request;

    /**
     * Login Endpoint
     * POST /api/auth/login
     * JWT Token döndürür
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        String ipAddress = getClientIpAddress(request);
        try {
            UserResponseDTO userResponse = userService.login(loginRequest);
            String token = jwtTokenProvider.generateToken(userResponse.getUsername());


            userService.findByUsername(userResponse.getUsername()).ifPresent(userEntity -> {
                accessLogService.createAccessLog(userEntity, "/api/auth/login", "POST",
                        AccessStatus.ALLOWED, "Login successful", ipAddress);
            });

            LoginResponseDTO response = new LoginResponseDTO(
                    "Login successful",
                    userResponse,
                    true,
                    token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            userService.findByUsername(loginRequest.getUsername()).ifPresent(user -> {
                accessLogService.createAccessLog(user, "/api/auth/login", "POST",
                        AccessStatus.DENIED, "Login failed: " + e.getMessage(), ipAddress);
            });

            LoginResponseDTO response = new LoginResponseDTO(
                    e.getMessage(),
                    null,
                    false,
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
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
