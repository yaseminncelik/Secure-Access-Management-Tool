package com.secureaccess.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AccessLog Entity - Tüm URL erişimlerini kaydeden entity
 * OOP Prensipleri: Encapsulation, Inheritance (BaseEntity'den türer)
 */
@Entity
@Table(name = "access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String requestedUrl;

    @Column(nullable = false, length = 50)
    private String httpMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status;

    @Column(length = 255)
    private String reason;

    @Column(length = 50)
    private String ipAddress;
}