package com.secureaccess.entity;

/**
 * Role ENUM - Encapsulation and Type Safety
 */
public enum Role {
    ADMIN("Admin Role"),
    USER("User Role");
    
    private final String description;
    
    Role(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
