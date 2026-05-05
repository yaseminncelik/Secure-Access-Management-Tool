package com.secureaccess.repository;

import com.secureaccess.entity.AccessLog;
import com.secureaccess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AccessLogRepository - AccessLog veri tabanı işlemleri
 * OOP Prensipleri: Abstraction (Repository pattern)
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    /**
     * Belirli bir kullanıcıya ait tüm access log'larını getir
     */
    List<AccessLog> findByUser(User user);

    /**
     * Belirli bir kullanıcının son access log'larını getir (sıralı)
     */
    List<AccessLog> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Belirli bir kullanıcıya ait access log'larını ID'ye göre getir (sıralı)
     */
    List<AccessLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
