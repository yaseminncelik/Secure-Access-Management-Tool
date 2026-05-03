package com.secureaccess.controller;
import com.secureaccess.dto.URLWhitelistRequestDTO;
import com.secureaccess.dto.URLWhitelistResponseDTO;
import com.secureaccess.entity.Role;
import com.secureaccess.entity.User;
import com.secureaccess.exception.UserNotFoundException;
import com.secureaccess.repository.UserRepository;
import com.secureaccess.security.JwtTokenProvider;
import com.secureaccess.service.URLWhitelistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WhitelistController - URL whitelist yönetimi
 * OOP Prensipleri: Encapsulation
 * Admin'ler tüm whitelist'leri görebilir, User'lar sadece kendi
 * whitelist'lerini
 */
@RestController
@RequestMapping("/api/whitelist")
public class WhitelistController {

    private final URLWhitelistService whitelistService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public WhitelistController(URLWhitelistService whitelistService,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository) {
        this.whitelistService = whitelistService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * Whitelist'e yeni URL ekle
     */
    @PostMapping("/add")
    public ResponseEntity<?> addWhitelistUrl(
            @RequestBody URLWhitelistRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getUserFromToken(authHeader);
            URLWhitelistResponseDTO response = whitelistService.addWhitelistUrl(user, requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Whitelist'ten URL sil
     */
    @DeleteMapping("/{whitelistId}")
    public ResponseEntity<?> removeWhitelistUrl(
            @PathVariable @org.springframework.lang.NonNull Long whitelistId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            getUserFromToken(authHeader); // Sadece token doğrulaması için çağırıyoruz
            whitelistService.removeWhitelistUrl(whitelistId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Whitelist URL başarıyla silindi");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Kullanıcının whitelist URL'lerini getir
     */
    @GetMapping("/my-urls")
    public ResponseEntity<?> getMyWhitelistUrls(
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getUserFromToken(authHeader);
            List<URLWhitelistResponseDTO> urls = whitelistService.getWhitelistUrls(user);
            return ResponseEntity.ok(urls);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Tüm whitelist URL'lerini getir (Admin only)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllWhitelistUrls(
            @RequestHeader("Authorization") String authHeader) {

        try {
            User user = getUserFromToken(authHeader);

            // Sadece Admin erişebilir
            if (!user.getRole().equals(Role.ADMIN)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bu işlem için Admin yetkisi gereklidir");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<URLWhitelistResponseDTO> urls = whitelistService.getAllWhitelistUrls();
            return ResponseEntity.ok(urls);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Belirli bir kullanıcının URL'lerini getir (Admin only)
     */
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<?> getWhitelistUrlsForUser(
            @PathVariable @org.springframework.lang.NonNull Long userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User adminUser = getUserFromToken(authHeader);
            if (!adminUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin yetkisi gereklidir"));
            }
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı"));
            List<URLWhitelistResponseDTO> urls = whitelistService.getWhitelistUrls(targetUser);
            return ResponseEntity.ok(urls);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Belirli bir kullanıcıya whitelist ekle (Admin only)
     */
    @PostMapping("/admin/add/{userId}")
    public ResponseEntity<?> addWhitelistUrlForUser(
            @PathVariable @org.springframework.lang.NonNull Long userId,
            @RequestBody URLWhitelistRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User adminUser = getUserFromToken(authHeader);
            if (!adminUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin yetkisi gereklidir"));
            }
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı"));
            URLWhitelistResponseDTO response = whitelistService.addWhitelistUrl(targetUser, requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Token'dan User objesini al
     */
    private User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Geçersiz authorization header");
        }

        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + username));
    }
}
