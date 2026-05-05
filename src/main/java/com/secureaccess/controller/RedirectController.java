package com.secureaccess.controller;

import com.secureaccess.dto.RedirectRequestDTO;
import com.secureaccess.dto.RedirectResponseDTO;
import com.secureaccess.entity.AccessStatus;
import com.secureaccess.entity.User;
import com.secureaccess.security.JwtAuthenticationProvider;
import com.secureaccess.service.AccessLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * RedirectController - Erişim kontrolü yapan ana modül.
 * NDP Notu: Encapsulation (Kapsülleme) - DTO kullanımıyla veriler kontrollü bir şekilde paketlenip aktarılır.
 */
@RestController
@RequestMapping("/api/redirect")
public class RedirectController {

    private final JwtAuthenticationProvider authProvider;
    private final AccessLogService accessLogService;
    private final com.secureaccess.repository.UserRepository userRepository;

    public RedirectController(JwtAuthenticationProvider authProvider,
            AccessLogService accessLogService,
            com.secureaccess.repository.UserRepository userRepository) {
        this.authProvider = authProvider;
        this.accessLogService = accessLogService;
        this.userRepository = userRepository;
    }

    /**
     * URL redirect isteği
     * Token'ı Authorization header'dan oku ve kontrol et
     */
    @PostMapping("/url")
    public ResponseEntity<RedirectResponseDTO> redirectToUrl(
            @RequestBody RedirectRequestDTO redirectRequest,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);
        String token = extractTokenFromHeader(authHeader);
        String url = redirectRequest.getUrl();
        String httpMethod = redirectRequest.getHttpMethod() != null ? redirectRequest.getHttpMethod() : "GET";

        if (token == null) {
            logAccessAttempt(null, url, httpMethod, AccessStatus.INVALID_TOKEN,
                    "Token sağlanmadı", ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RedirectResponseDTO(false, "Token sağlanmadı", null));
        }

        User user = authProvider.authenticate(token);
        if (user == null) {
            logAccessAttempt(null, url, httpMethod, AccessStatus.INVALID_TOKEN,
                    "Geçersiz token", ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RedirectResponseDTO(false, "Geçersiz token", null));
        }

        if (authProvider.isTokenExpired(token)) {
            logAccessAttempt(user, url, httpMethod, AccessStatus.EXPIRED_TOKEN,
                    "Token süresi dolmuş", ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RedirectResponseDTO(false, "Token süresi dolmuş", null));
        }

        User targetUser = user;
        Long testUserId = redirectRequest.getUserId();
        if (user.getRole().toString().equals("ADMIN") && testUserId != null) {
            targetUser = userRepository.findById(testUserId).orElse(user);
        }

        if (!authProvider.authorize(targetUser, url, httpMethod)) {
            logAccessAttempt(targetUser, url, httpMethod, AccessStatus.DENIED,
                    "Bu URL'e erişim izniniz yok", ipAddress);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RedirectResponseDTO(false, "Bu URL'e erişim izniniz yok", null));
        }

        logAccessAttempt(targetUser, url, httpMethod, AccessStatus.ALLOWED,
                "Erişime izin verildi", ipAddress);

        return ResponseEntity.ok()
                .body(new RedirectResponseDTO(true, "Erişime izin verildi", url));
    }

    /**
     * Token'dan Authorization header'ı çıkart
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    /**
     * Client'ın IP adresini al
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private void logAccessAttempt(User user, String url, String httpMethod,
            AccessStatus status, String reason, String ipAddress) {
        if (user != null) {
            accessLogService.createAccessLog(user, url, httpMethod, status, reason, ipAddress);
        }
    }
}
