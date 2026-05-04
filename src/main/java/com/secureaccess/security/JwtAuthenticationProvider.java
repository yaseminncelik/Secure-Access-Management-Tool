package com.secureaccess.security;

import com.secureaccess.entity.User;
import com.secureaccess.repository.URLWhitelistRepository;
import com.secureaccess.repository.UserRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * JwtAuthenticationProvider - JWT token doğrulama ve yetkilendirme.
 * NDP Notu: Abstraction (Soyutlama) prensibi burada güvenlik mantığının 
 * tek bir sınıfta toplanmasıyla sağlanmıştır.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider extends AbstractAuthenticationProvider {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final URLWhitelistRepository whitelistRepository;

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
