package com.secureaccess.controller;

import com.secureaccess.dto.RedirectRequestDTO;
import com.secureaccess.dto.RedirectResponseDTO;
import com.secureaccess.entity.AccessStatus;
import com.secureaccess.entity.User;
import com.secureaccess.security.JwtAuthenticationProvider;
import com.secureaccess.service.AccessLogService;
import com.secureaccess.service.URLWhitelistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * RedirectController - Firewall/Redirect servisi
 * OOP Prensipleri: Encapsulation, Abstraction
 * Kullanıcılar URL'i bu endpoint'e göndererek redirect sevisi kullanabilir
 */
@RestController
@RequestMapping("/api/redirect")
public class RedirectController {

    private final JwtAuthenticationProvider authProvider;
    private final AccessLogService accessLogService;
    private final URLWhitelistService whitelistService;

    public RedirectController(JwtAuthenticationProvider authProvider,
            AccessLogService accessLogService,
            URLWhitelistService whitelistService) {
        this.authProvider = authProvider;
        this.accessLogService = accessLogService;
        this.whitelistService = whitelistService;
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

        // Token'ı doğrula ve kullanıcıyı al
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

        // Token'ın süresi dolmuş mı kontrol et
        if (authProvider.isTokenExpired(token)) {
            logAccessAttempt(user, url, httpMethod, AccessStatus.EXPIRED_TOKEN,
                    "Token süresi dolmuş", ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RedirectResponseDTO(false, "Token süresi dolmuş", null));
        }

        // Authorization kontrol et (whitelist)
        if (!authProvider.authorize(user, url, httpMethod)) {
            logAccessAttempt(user, url, httpMethod, AccessStatus.DENIED,
                    "Bu URL'e erişim izniniz yok", ipAddress);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RedirectResponseDTO(false, "Bu URL'e erişim izniniz yok", null));
        }

        // Erişime izin ver ve log kaydı oluştur
        logAccessAttempt(user, url, httpMethod, AccessStatus.ALLOWED,
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

    /**
     * Access log kaydı oluştur
     */
    private void logAccessAttempt(User user, String url, String httpMethod,
            AccessStatus status, String reason, String ipAddress) {
        // Eğer user null ise, log oluşturamayız (DB'de user_id zorunlu)
        // Gerçek uygulamada, başarısız denemeler için farklı bir tablo olabilir
        if (user != null) {
            accessLogService.createAccessLog(user, url, httpMethod, status, reason, ipAddress);
        }
    }
}
