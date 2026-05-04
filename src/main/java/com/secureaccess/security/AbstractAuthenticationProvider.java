package com.secureaccess.security;

import com.secureaccess.entity.User;

/**
 * AbstractAuthenticationProvider - Güvenlik işlemlerinin temel yapısı.
 * NDP Notu: Abstraction (Soyutlama) ve Inheritance (Kalıtım). 
 * Bu sınıf abstract olarak tanımlanmış ve temel iskeleti belirlemiştir.
 * Alt sınıflar bu metotları doldurmak zorundadır.
 */
public abstract class AbstractAuthenticationProvider {

    /**
     * Token'ı doğrula
     */
    public abstract User authenticate(String token);

    /**
     * Kullanıcının belirli URL'e erişme yetkisine sahip olup olmadığını kontrol et
     */
    public abstract boolean authorize(User user, String requestedUrl, String httpMethod);

    /**
     * Token'dan username'i çıkart
     */
    public abstract String extractUsername(String token);

    /**
     * Token'ın süresi dolmuş olup olmadığını kontrol et
     */
    public abstract boolean isTokenExpired(String token);

    /**
     * Token'ın geçerliliğini kontrol et (format, signature, expiration)
     */
    public abstract boolean isTokenValid(String token);

    /**
     * Template Method - Login flow'unun temel yapısını tanımla
     * Concrete implementasyonlar bu method'ları override edecek
     */
    public final String getAuthenticationFlow(String token, String requestedUrl, String httpMethod) {
        if (!isTokenValid(token)) {
            return "INVALID_TOKEN";
        }

        if (isTokenExpired(token)) {
            return "EXPIRED_TOKEN";
        }

        User user = authenticate(token);
        if (user == null) {
            return "AUTHENTICATION_FAILED";
        }

        if (!authorize(user, requestedUrl, httpMethod)) {
            return "AUTHORIZATION_FAILED";
        }

        return "SUCCESS";
    }
}
