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
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserServiceImpl - UserService'in implementation'ı.
 * NDP Notu: Abstraction (Soyutlama) prensibi, UserService arayüzü sayesinde
 * iş mantığının gizlenmesi ve bağımlılıkların yönetilmesiyle uygulanmıştır.
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
    public UserResponseDTO getUserById(@NonNull Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        validatePassword(requestDTO.getPassword());

        if (requestDTO.getRole().toString().equals("ADMIN") && !"admin".equals(requestDTO.getUsername())) {
            throw new IllegalArgumentException("Admin kullanıcısının adı sadece 'admin' olabilir");
        }

        if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + requestDTO.getUsername());
        }

        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + requestDTO.getEmail());
        }

        User user = new User(
                requestDTO.getUsername(),
                passwordEncoder.encode(requestDTO.getPassword()),
                requestDTO.getEmail(),
                requestDTO.getRole());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(@NonNull Long id, UserRequestDTO requestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (requestDTO.getRole().toString().equals("ADMIN") && !"admin".equals(requestDTO.getUsername())) {
            throw new IllegalArgumentException("Admin kullanıcısının adı sadece 'admin' olabilir");
        }

        if (!user.getUsername().equals(requestDTO.getUsername())) {
            if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username already exists: " + requestDTO.getUsername());
            }
        }

        if (!user.getEmail().equals(requestDTO.getEmail())) {
            if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Email already exists: " + requestDTO.getEmail());
            }
        }

        user.setUsername(requestDTO.getUsername());
        
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().trim().isEmpty()) {
            validatePassword(requestDTO.getPassword());
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }
        
        user.setEmail(requestDTO.getEmail());
        user.setRole(requestDTO.getRole());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public void deleteUser(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        String identifier = loginRequest.getUsername();
        User user = null;

        if ("admin".equals(identifier)) {
            user = userRepository.findByUsername("admin").orElse(null);
        } else {
            if (identifier != null && identifier.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                user = userRepository.findByEmail(identifier).orElse(null);
                if (user != null && user.getRole().toString().equals("ADMIN")) {
                    user = null; // Admin hesabı email ile giriş yapamaz
                }
            }
        }

        if (user == null) {
            throw new InvalidCredentialsException("Geçersiz giriş bilgileri");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Geçersiz giriş bilgileri");
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Geçersiz giriş bilgileri");
        }

        return convertToDTO(user);
    }


    @Override
    public void resetPassword(String username, String email, String newPassword) {
        User user = userRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new InvalidCredentialsException("Kullanıcı adı veya e-posta hatalı"));

        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
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

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Şifre boş olamaz");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Şifre en az 8 karakter olmalıdır");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Şifre en az 1 büyük harf içermelidir");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Şifre en az 1 küçük harf içermelidir");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Şifre en az 1 rakam içermelidir");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new IllegalArgumentException("Şifre en az 1 özel karakter içermelidir");
        }
    }
}
