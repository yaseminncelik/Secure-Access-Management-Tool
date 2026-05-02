package com.secureaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RedirectResponseDTO - Redirect servisi cevabı
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedirectResponseDTO {

    private Boolean success;

    private String message;

    private String redirectUrl;
}
