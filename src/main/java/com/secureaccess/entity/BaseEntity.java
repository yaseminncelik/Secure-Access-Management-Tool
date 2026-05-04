package com.secureaccess.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * BaseEntity - Ortak özellikleri barındıran temel sınıf.
 * NDP Notu: Inheritance (Kalıtım) - Diğer tüm entity sınıfları (User, Whitelist vb.)
 * bu sınıftan türeyerek id, createdAt gibi özellikleri miras alır.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
