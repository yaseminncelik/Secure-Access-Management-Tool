package com.secureaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RedirectRequestDTO - Redirect servisi isteği
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedirectRequestDTO {

    private String url;

    private String httpMethod; // Optional, default: GET
}
