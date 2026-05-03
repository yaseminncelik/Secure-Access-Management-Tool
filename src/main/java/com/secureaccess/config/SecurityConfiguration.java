package com.secureaccess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * SecurityConfiguration - Spring Security yapılandırması
 * OOP Prensipleri: Encapsulation
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Password Encoder Bean - Şifre şifrelemesi için
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain - Güvenlik kuralları
     * Custom controller'lardaki auth mantığını kullanabilmek için
     * Spring Security'nin default kimlik doğrulamasını (form login / basic auth) devreden çıkarıyoruz.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF'yi devreden çıkarıyoruz (Stateless REST API için gerekli)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Tüm yetkilendirmeyi kendi Controller'larımız üzerinden (manuel token kontrolü) yapıyoruz
            );
        
        return http.build();
    }
}
