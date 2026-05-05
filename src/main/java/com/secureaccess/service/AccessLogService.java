package com.secureaccess.service;

import com.secureaccess.entity.AccessLog;
import com.secureaccess.entity.AccessStatus;
import com.secureaccess.entity.User;

import java.util.List;

/**
 * AccessLogService Interface - Access log işlemleri
 * OOP Prensipleri: Polymorphism, Abstraction
 */
public interface AccessLogService {

    /**
     * Yeni access log oluştur ve kaydet
     */
    AccessLog createAccessLog(User user, String requestedUrl, String httpMethod,
            AccessStatus status, String reason, String ipAddress);

    /**
     * Belirli bir kullanıcının access log'larını getir
     */
    List<AccessLog> getAccessLogsByUser(User user);

    /**
     * Belirli bir kullanıcının son access log'larını getir
     */
    List<AccessLog> getRecentAccessLogs(User user);

    /**
     * Belirli bir kullanıcının access log'larını ID'ye göre getir
     */
    List<AccessLog> getAccessLogsByUserId(Long userId);

    /**
     * Tüm access log'larını getir
     */
    List<AccessLog> getAllAccessLogs();
}
