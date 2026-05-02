package com.secureaccess.entity;

/**
 * AccessStatus ENUM - Erişim sonucunu gösteren enum
 */
public enum AccessStatus {
    ALLOWED("Erişime izin verildi"),
    DENIED("Erişim reddedildi"),
    INVALID_TOKEN("Geçersiz token"),
    EXPIRED_TOKEN("Token süresi doldu");

    private final String description;

    AccessStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
