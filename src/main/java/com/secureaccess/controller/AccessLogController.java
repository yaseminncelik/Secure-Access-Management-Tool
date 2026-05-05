package com.secureaccess.controller;

import com.secureaccess.dto.AccessLogDTO;
import com.secureaccess.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AccessLogController - Erişim kayıtlarını görüntüleme.
 * OOP Prensipleri:
 * - Abstraction (Soyutlama): AccessLogService interface'i aracılığıyla iş
 * mantığı gizlenir
 * - Encapsulation (Kapsülleme): Sadece public metotlar erişilebilir, iç
 * detaylar gizlenmiş
 */
@RestController
@RequestMapping("/api/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    /**
     * Tüm access log kayıtlarını getir (Admin only)
     * GET /api/access-logs/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<AccessLogDTO>> getAllAccessLogs() {
        List<AccessLogDTO> logs = accessLogService.getAllAccessLogs()
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(logs);
    }

    /**
     * Belirli bir kullanıcının access log'larını getir
     * GET /api/access-logs/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccessLogDTO>> getUserAccessLogs(@PathVariable Long userId) {
        List<AccessLogDTO> logs = accessLogService.getAccessLogsByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(logs);
    }

    /**
     * Dto conversion helper method
     */
    private AccessLogDTO convertToDTO(com.secureaccess.entity.AccessLog log) {
        return new AccessLogDTO(
                log.getId(),
                log.getUser().getUsername(),
                log.getRequestedUrl(),
                log.getHttpMethod(),
                log.getStatus().toString(),
                log.getReason(),
                log.getIpAddress(),
                log.getCreatedAt().toString());
    }
}
