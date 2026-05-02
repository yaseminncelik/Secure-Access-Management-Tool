package com.secureaccess.security;

import com.secureaccess.entity.User;
import com.secureaccess.repository.URLWhitelistRepository;
import com.secureaccess.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * JwtAuthenticationProvider - JWT tabanlı authentication ve authorization
 * OOP Prensipleri: Inheritance, Polymorphism
 * Concrete implementation of AbstractAuthenticationProvider
 */
@Component
public class JwtAuthenticationProvider extends AbstractAuthenticationProvider {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final URLWhitelistRepository whitelistRepository;

    public JwtAuthenticationProvider(JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            URLWhitelistRepository whitelistRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.whitelistRepository = whitelistRepository;
    }

    /**
     * Token'ı doğrula ve User objesini döndür
     */
    @Override
    public User authenticate(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            return userRepository.findByUsername(username)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kullanıcının belirli URL'e erişim yetkisine sahip olup olmadığını kontrol et
     * Admin ise tüm URL'lere erişim hakkı vardır
     * Diğer kullanıcılar sadece whitelist'deki URL'lere erişebilir
     */
    @Override
    public boolean authorize(User user, String requestedUrl, String httpMethod) {
        if (user == null || user.getRole().toString().equals("ADMIN")) {
            return true;
        }

        // Kullanıcı tarafından tanımlanan whitelist kontrol et
        return whitelistRepository.existsByUserAndUrlAndHttpMethod(
                user, requestedUrl, httpMethod);
    }

    /**
     * Token'dan username'i çıkart
     */
    @Override
    public String extractUsername(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * Token'ın süresi dolmuş olup olmadığını kontrol et
     */
    @Override
    public boolean isTokenExpired(String token) {
        return jwtTokenProvider.isTokenExpired(token);
    }

    /**
     * Token'ın geçerliliğini kontrol et
     */
    @Override
    public boolean isTokenValid(String token) {
        try {
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
