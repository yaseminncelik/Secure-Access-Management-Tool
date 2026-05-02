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
     * Belirli bir kullanıcının son 100 access log'unu getir (sıralı)
     */
    List<AccessLog> findByUserOrderByCreatedAtDesc(User user);
}
