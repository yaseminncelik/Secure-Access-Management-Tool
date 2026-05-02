package com.secureaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * URLWhitelistResponseDTO - Whitelist URL cevabı
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class URLWhitelistResponseDTO {

    private Long id;

    private String username;

    private String url;

    private String httpMethod;

    private String description;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
