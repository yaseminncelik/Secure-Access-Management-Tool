package com.secureaccess.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * URLWhitelist Entity - İzinli URL bilgilerini tutar.
 * NDP Notu: Encapsulation (Kapsülleme) - Erişim kısıtlamaları burada uygulanmıştır.
 */
@Entity
@Table(name = "url_whitelist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class URLWhitelist extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "URL cannot be blank")
    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false, length = 50)
    private String httpMethod; // GET, POST, PUT, DELETE, etc.

    @NotBlank(message = "Description cannot be blank")
    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;
}