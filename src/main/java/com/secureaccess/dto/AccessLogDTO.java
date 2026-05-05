package com.secureaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AccessLogDTO - Access log verilerini transfer etmek için.
 * OOP Prensipleri:
 * - Encapsulation (Kapsülleme): @Getter ile verilere kontrollü erişim
 */
@Getter
@AllArgsConstructor
public class AccessLogDTO {
    private Long id;
    private String username;
    private String requestedUrl;
    private String httpMethod;
    private String status;
    private String reason;
    private String ipAddress;
    private String createdAt;
}
