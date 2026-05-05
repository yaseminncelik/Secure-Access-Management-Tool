package com.secureaccess.service;

import com.secureaccess.entity.AccessLog;
import com.secureaccess.entity.AccessStatus;
import com.secureaccess.entity.User;
import com.secureaccess.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AccessLogServiceImpl - Access log işlemleri implementation
 * OOP Prensipleri: Inheritance, Polymorphism
 */
@Service
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public AccessLogServiceImpl(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    @Override
    public AccessLog createAccessLog(User user, String requestedUrl, String httpMethod,
            AccessStatus status, String reason, String ipAddress) {
        AccessLog accessLog = new AccessLog(user, requestedUrl, httpMethod, status, reason, ipAddress);
        return accessLogRepository.save(accessLog);
    }

    @Override
    public List<AccessLog> getAccessLogsByUser(User user) {
        return accessLogRepository.findByUser(user);
    }

    @Override
    public List<AccessLog> getRecentAccessLogs(User user) {
        return accessLogRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<AccessLog> getAccessLogsByUserId(Long userId) {
        return accessLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<AccessLog> getAllAccessLogs() {
        return accessLogRepository.findAll();
    }
}
