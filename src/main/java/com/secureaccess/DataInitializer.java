package com.secureaccess;

import com.secureaccess.entity.Role;
import com.secureaccess.entity.User;
import com.secureaccess.entity.URLWhitelist;
import com.secureaccess.repository.URLWhitelistRepository;
import com.secureaccess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * DataInitializer - Application başladığında demo veri oluştur
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initializeData(UserRepository userRepository,
            URLWhitelistRepository whitelistRepository) {
        return args -> {
            // Admin kullanıcı
            User admin = null;
            if (userRepository.findByUsername("admin").isEmpty()) {
                admin = new User("admin", passwordEncoder.encode("password"),
                        "admin@example.com", Role.ADMIN);
                userRepository.save(admin);
                System.out.println("✓ Admin kullanıcı oluşturuldu: admin / password");
            } else {
                admin = userRepository.findByUsername("admin").get();
            }

            // Normal kullanıcılar
            User user1 = null;
            if (userRepository.findByUsername("user1").isEmpty()) {
                user1 = new User("user1", passwordEncoder.encode("password123"),
                        "user1@example.com", Role.USER);
                userRepository.save(user1);
                System.out.println("✓ Kullanıcı oluşturuldu: user1 / password123");
            } else {
                user1 = userRepository.findByUsername("user1").get();
            }

            User user2 = null;
            if (userRepository.findByUsername("user2").isEmpty()) {
                user2 = new User("user2", passwordEncoder.encode("password456"),
                        "user2@example.com", Role.USER);
                userRepository.save(user2);
                System.out.println("✓ Kullanıcı oluşturuldu: user2 / password456");
            } else {
                user2 = userRepository.findByUsername("user2").get();
            }

            // URLWhitelist - user1 için izinli URL'ler
            if (whitelistRepository.findByUserAndIsActiveTrue(user1).isEmpty()) {
                URLWhitelist whitelist1 = new URLWhitelist();
                whitelist1.setUser(user1);
                whitelist1.setUrl("https://www.google.com");
                whitelist1.setHttpMethod("GET");
                whitelist1.setDescription("Google arama sitesi");
                whitelist1.setIsActive(true);
                whitelistRepository.save(whitelist1);

                URLWhitelist whitelist2 = new URLWhitelist();
                whitelist2.setUser(user1);
                whitelist2.setUrl("https://www.github.com");
                whitelist2.setHttpMethod("GET");
                whitelist2.setDescription("GitHub repository");
                whitelist2.setIsActive(true);
                whitelistRepository.save(whitelist2);

                System.out.println("✓ user1 için whitelist URL'leri oluşturuldu");
            }

            // URLWhitelist - user2 için izinli URL'ler
            if (whitelistRepository.findByUserAndIsActiveTrue(user2).isEmpty()) {
                URLWhitelist whitelist3 = new URLWhitelist();
                whitelist3.setUser(user2);
                whitelist3.setUrl("https://www.stackoverflow.com");
                whitelist3.setHttpMethod("GET");
                whitelist3.setDescription("Stack Overflow");
                whitelist3.setIsActive(true);
                whitelistRepository.save(whitelist3);

                URLWhitelist whitelist4 = new URLWhitelist();
                whitelist4.setUser(user2);
                whitelist4.setUrl("https://www.linkedin.com");
                whitelist4.setHttpMethod("GET");
                whitelist4.setDescription("LinkedIn");
                whitelist4.setIsActive(true);
                whitelistRepository.save(whitelist4);

                System.out.println("✓ user2 için whitelist URL'leri oluşturuldu");
            }

            System.out.println("\n========== DEMO KULLANICILAR ==========");
            System.out.println("Admin: admin / password");
            System.out.println("User1: user1 / password123");
            System.out.println("User2: user2 / password456");
            System.out.println("\n========== ENDPOINTS ==========");
            System.out.println("1. Login: POST /api/auth/login");
            System.out.println("2. Redirect: POST /api/redirect/url");
            System.out.println("3. Whitelist Yönetimi: /api/whitelist/*");
            System.out.println("4. H2 Console: http://localhost:8080/h2-console");
        };
    }
}
