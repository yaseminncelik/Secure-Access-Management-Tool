package com.secureaccess.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User Entity - Kullanıcı bilgilerini tutar.
 * NDP Notu: Encapsulation (Kapsülleme) - private fieldlar ve getter/setterlar ile veriye kontrollü erişim.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    
    @NotBlank(message = "Username cannot be blank")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Column(unique = true, nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    /**
     * Constructor - Kullanıcı oluşturma için minimal constructor
     */
    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.isActive = true;
    }
}
