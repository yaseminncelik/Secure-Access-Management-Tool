package com.secureaccess.service;

import com.secureaccess.dto.LoginRequestDTO;
import com.secureaccess.dto.UserRequestDTO;
import com.secureaccess.dto.UserResponseDTO;
import com.secureaccess.entity.User;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * UserService Interface - Polymorphism ve Abstraction
 * Service katmanı tüm business logic'i içerir
 */
public interface UserService {
    
    /**
     * Tüm kullanıcıları getir
     */
    List<UserResponseDTO> getAllUsers();
    
    /**
     * ID'ye göre kullanıcı getir
     */
    UserResponseDTO getUserById(@NonNull Long id);
    
    /**
     * Yeni kullanıcı oluştur
     */
    UserResponseDTO createUser(UserRequestDTO requestDTO);
    
    /**
     * Kullanıcı güncelle
     */
    UserResponseDTO updateUser(@NonNull Long id, UserRequestDTO requestDTO);
    
    /**
     * Kullanıcı sil
     */
    void deleteUser(@NonNull Long id);
    
    /**
     * Login işlemi
     */
    UserResponseDTO login(LoginRequestDTO loginRequest);

    /**
     * Şifre sıfırlama işlemi
     */
    void resetPassword(String username, String email, String newPassword);

    /**
     * Kullanıcı adıyla kullanıcı getir (Entity döner - internal kullanım için)
     */
    Optional<User> findByUsername(String username);
}
