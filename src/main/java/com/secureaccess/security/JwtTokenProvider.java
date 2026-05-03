package com.secureaccess.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtTokenProvider - JWT token oluşturma, doğrulama ve parsing işlemleri
 * OOP Prensipleri: Encapsulation, Single Responsibility
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-change-this-in-production-environment-with-at-least-256-bits}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 saat
    private long jwtExpiration;

    /**
     * JWT token oluştur
     */
    public String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Token'dan username'i çıkart
     */
    public String getUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Token'dan username çıkarılamadı", e);
        }
    }

    /**
     * Token'ı doğrula
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Geçersiz JWT token format", e);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token süresi dolmuş", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("JWT token desteklenmiyor", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT token boş", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new RuntimeException("JWT token imzası doğrulanamadı", e);
        }
    }

    /**
     * Token'ın süresi dolmuş olup olmadığını kontrol et
     */
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Date expirationDate = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expirationDate.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Token'ı parse et ve Claims'i getir
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Claims çıkarılamadı", e);
        }
    }
}
