package com.secureaccess.repository;

import com.secureaccess.entity.URLWhitelist;
import com.secureaccess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * URLWhitelistRepository - URLWhitelist veri tabanı işlemleri
 * OOP Prensipleri: Abstraction (Repository pattern)
 */
@Repository
public interface URLWhitelistRepository extends JpaRepository<URLWhitelist, Long> {

    /**
     * Belirli bir kullanıcıya ait tüm whitelist URL'lerini getir
     */
    List<URLWhitelist> findByUserAndIsActiveTrue(User user);

    /**
     * Belirli bir URL'in belirli bir method için whitelist'te olup olmadığını
     * kontrol et
     */
    boolean existsByUserAndUrlAndHttpMethod(User user, String url, String httpMethod);

    /**
     * Admin tarafından tanımlanan tüm active whitelist URL'lerini getir
     */
    List<URLWhitelist> findByIsActiveTrueOrderByCreatedAtDesc();
}
