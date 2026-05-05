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

        // URL'leri normalize et ve karşılaştır
        String normalizedRequestedUrl = normalizeUrl(requestedUrl);

        // Tüm whitelist URL'lerini getir ve normalize ederek karşılaştır
        var whitelistUrls = whitelistRepository.findByUserAndIsActiveTrue(user);
        return whitelistUrls.stream()
                .anyMatch(whitelist -> normalizeUrl(whitelist.getUrl()).equals(normalizedRequestedUrl) &&
                        whitelist.getHttpMethod().equals(httpMethod));
    }

    /**
     * URL'i normalize et (www'siz, trailing slash'siz, lowercase domain)
     * Örnek: https://www.github.com/ -> https://github.com
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost().toLowerCase();
            String path = parsedUrl.getPath();

            // www. ön ekini kaldır (sadece subdomain ise)
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // Trailing slash'i kaldır (sadece path'i yoksa)
            if (path.equals("/")) {
                path = "";
            }

            return protocol + "://" + host + path;
        } catch (java.net.MalformedURLException e) {
            // URL geçersizse, olduğu gibi döndür
            return url;
        }
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
