package com.secureaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * URLWhitelistRequestDTO - Whitelist URL ekleme isteği
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class URLWhitelistRequestDTO {

    private String url;

    private String httpMethod; // GET, POST, PUT, DELETE, etc.

    private String description;
}
