package com.secureaccess.service;

import com.secureaccess.dto.URLWhitelistRequestDTO;
import com.secureaccess.dto.URLWhitelistResponseDTO;
import com.secureaccess.entity.URLWhitelist;
import com.secureaccess.entity.User;
import com.secureaccess.exception.UserNotFoundException;
import com.secureaccess.repository.URLWhitelistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

/**
 * URLWhitelistServiceImpl - URL whitelist işlemleri implementation
 * OOP Prensipleri: Inheritance, Polymorphism
 */
@Service
public class URLWhitelistServiceImpl implements URLWhitelistService {

    private final URLWhitelistRepository whitelistRepository;

    public URLWhitelistServiceImpl(URLWhitelistRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
    }

    @Override
    public URLWhitelistResponseDTO addWhitelistUrl(User user, URLWhitelistRequestDTO requestDTO) {
        if (user == null) {
            throw new UserNotFoundException("Kullanıcı bulunamadı");
        }

        URLWhitelist whitelist = new URLWhitelist();
        whitelist.setUser(user);
        whitelist.setUrl(requestDTO.getUrl());
        whitelist.setHttpMethod(requestDTO.getHttpMethod());
        whitelist.setDescription(requestDTO.getDescription());
        whitelist.setIsActive(true);

        URLWhitelist savedWhitelist = whitelistRepository.save(whitelist);
        return convertToResponseDTO(savedWhitelist);
    }

    @Override
    public void removeWhitelistUrl(@NonNull Long whitelistId) {
        whitelistRepository.deleteById(whitelistId);
    }

    @Override
    public List<URLWhitelistResponseDTO> getWhitelistUrls(User user) {
        List<URLWhitelist> whitelists = whitelistRepository.findByUserAndIsActiveTrue(user);
        return whitelists.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUrlWhitelisted(User user, String url, String httpMethod) {
        return whitelistRepository.existsByUserAndUrlAndHttpMethod(user, url, httpMethod);
    }

    @Override
    public List<URLWhitelistResponseDTO> getAllWhitelistUrls() {
        List<URLWhitelist> whitelists = whitelistRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return whitelists.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * URLWhitelist entity'yi ResponseDTO'ya dönüştür
     */
    private URLWhitelistResponseDTO convertToResponseDTO(URLWhitelist whitelist) {
        return new URLWhitelistResponseDTO(
                whitelist.getId(),
                whitelist.getUser().getUsername(),
                whitelist.getUrl(),
                whitelist.getHttpMethod(),
                whitelist.getDescription(),
                whitelist.getIsActive(),
                whitelist.getCreatedAt());
    }
}
