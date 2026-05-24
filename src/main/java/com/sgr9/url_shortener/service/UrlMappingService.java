package com.sgr9.url_shortener.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.sgr9.url_shortener.dto.UrlMappingDTO;
import com.sgr9.url_shortener.models.UrlMapping;
import com.sgr9.url_shortener.models.User;
import com.sgr9.url_shortener.repository.UrlMappingRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class UrlMappingService {

    private UrlMappingRepository urlMappingRepository;

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalURL(originalUrl);
        urlMapping.setShortURL(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedData(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrlMapping);
    }

    private UrlMappingDTO convertToDto(UrlMapping urlMapping){
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalURL());
        urlMappingDTO.setShortUrl(urlMapping.getShortURL());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreatedDate(urlMapping.getCreatedData());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }

    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }
}
