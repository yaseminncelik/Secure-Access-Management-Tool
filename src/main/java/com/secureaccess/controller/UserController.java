package com.secureaccess.controller;

import com.secureaccess.dto.UserRequestDTO;
import com.secureaccess.dto.UserResponseDTO;
import com.secureaccess.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserController - Kullanıcı yönetimi (Ekleme, Silme, Listeleme).
 * NDP Notu: Abstraction (Soyutlama) - İş mantığı Service katmanındaki arayüzler arkasında gizlenmiştir.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Tüm kullanıcıları getir
     * GET /users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Users retrieved successfully");
        response.put("data", users);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ID'ye göre kullanıcı getir
     * GET /users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable @NonNull Long id) {
        UserResponseDTO user = userService.getUserById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User retrieved successfully");
        response.put("data", user);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Yeni kullanıcı oluştur
     * POST /users
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        UserResponseDTO user = userService.createUser(requestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User created successfully");
        response.put("data", user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Kullanıcı güncelle
     * PUT /users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {
        UserResponseDTO user = userService.updateUser(id, requestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User updated successfully");
        response.put("data", user);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kullanıcı sil
     * DELETE /users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable @NonNull Long id) {
        userService.deleteUser(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }
}
