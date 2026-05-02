package com.secureaccess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityConfiguration - Spring Security yapılandırması
 * OOP Prensipleri: Encapsulation
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Password Encoder Bean - Şifre şifrelemesi için
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
