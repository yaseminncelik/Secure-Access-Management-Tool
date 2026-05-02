package com.secureaccess.service;

import com.secureaccess.dto.LoginRequestDTO;
import com.secureaccess.dto.UserRequestDTO;
import com.secureaccess.dto.UserResponseDTO;

import java.util.List;

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
    UserResponseDTO getUserById(Long id);
    
    /**
     * Yeni kullanıcı oluştur
     */
    UserResponseDTO createUser(UserRequestDTO requestDTO);
    
    /**
     * Kullanıcı güncelle
     */
    UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO);
    
    /**
     * Kullanıcı sil
     */
    void deleteUser(Long id);
    
    /**
     * Login işlemi
     */
    UserResponseDTO login(LoginRequestDTO loginRequest);
}
