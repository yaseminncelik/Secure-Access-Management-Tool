package com.secureaccess.service;

import com.secureaccess.dto.LoginRequestDTO;
import com.secureaccess.dto.UserRequestDTO;
import com.secureaccess.dto.UserResponseDTO;
import com.secureaccess.entity.User;
import com.secureaccess.exception.InvalidCredentialsException;
import com.secureaccess.exception.UserAlreadyExistsException;
import com.secureaccess.exception.UserNotFoundException;
import com.secureaccess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserServiceImpl - UserService'in implementation'ı (Polymorphism)
 * Tüm business logic burada yer alır
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        // Kullanıcı zaten var mı kontrol et
        if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + requestDTO.getUsername());
        }

        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + requestDTO.getEmail());
        }

        // Yeni kullanıcı oluştur (Şifre encode edilmiş)
        User user = new User(
                requestDTO.getUsername(),
                passwordEncoder.encode(requestDTO.getPassword()),
                requestDTO.getEmail(),
                requestDTO.getRole());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Username kontrol (başka birinin username'ini alamamak için)
        if (!user.getUsername().equals(requestDTO.getUsername())) {
            if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username already exists: " + requestDTO.getUsername());
            }
        }

        // Email kontrol (başka birinin email'ini alamamak için)
        if (!user.getEmail().equals(requestDTO.getEmail())) {
            if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Email already exists: " + requestDTO.getEmail());
            }
        }

        user.setUsername(requestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setEmail(requestDTO.getEmail());
        user.setRole(requestDTO.getRole());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Geçersiz username veya password"));

        // Password encoder ile şifre kontrol et
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Geçersiz username veya password");
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Kullanıcı hesabı aktif değildir");
        }

        return convertToDTO(user);
    }

    /**
     * Entity'yi DTO'ya çevir (Encapsulation - sensitive bilgiler dışarı çıkmaz)
     */
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
