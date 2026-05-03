package com.secureaccess.service;

import com.secureaccess.dto.URLWhitelistRequestDTO;
import com.secureaccess.dto.URLWhitelistResponseDTO;
import com.secureaccess.entity.User;

import java.util.List;
import org.springframework.lang.NonNull;

/**
 * URLWhitelistService Interface - URL whitelist işlemleri
 * OOP Prensipleri: Polymorphism, Abstraction
 */
public interface URLWhitelistService {

    /**
     * Yeni whitelist URL ekle
     */
    URLWhitelistResponseDTO addWhitelistUrl(User user, URLWhitelistRequestDTO requestDTO);

    /**
     * Whitelist URL'ini sil
     */
    void removeWhitelistUrl(@NonNull Long whitelistId);

    /**
     * Belirli bir kullanıcının whitelist URL'lerini getir
     */
    List<URLWhitelistResponseDTO> getWhitelistUrls(User user);

    /**
     * Belirli bir kullanıcının URL'sine erişme yetkisi olup olmadığını kontrol et
     */
    boolean isUrlWhitelisted(User user, String url, String httpMethod);

    /**
     * Tüm active whitelist URL'lerini getir (Admin için)
     */
    List<URLWhitelistResponseDTO> getAllWhitelistUrls();
}
