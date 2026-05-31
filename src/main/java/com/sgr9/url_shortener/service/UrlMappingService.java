package com.sgr9.url_shortener.service;

import org.springframework.stereotype.Service;

import com.sgr9.url_shortener.dto.ClickEventDTO;
import com.sgr9.url_shortener.repository.ClickEventRepository;
import com.sgr9.url_shortener.dto.UrlMappingDTO;
import com.sgr9.url_shortener.models.ClickEvent;
import com.sgr9.url_shortener.models.UrlMapping;
import com.sgr9.url_shortener.models.User;
import com.sgr9.url_shortener.repository.UrlMappingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class UrlMappingService {
    private final ClickEventRepository clickEventRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final StringRedisTemplate redisTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public UrlMappingService(ClickEventRepository clickEventRepository, UrlMappingRepository urlMappingRepository, StringRedisTemplate redisTemplate) {
        this.clickEventRepository = clickEventRepository;
        this.urlMappingRepository = urlMappingRepository;
        this.redisTemplate = redisTemplate;
    }

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);

        // Cache the newly created URL mapping in Redis
        redisTemplate.opsForValue().set("url:" + shortUrl, originalUrl);

        return convertToDto(savedUrlMapping);
    }

    private UrlMappingDTO convertToDto(UrlMapping urlMapping){
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreatedDate(urlMapping.getCreatedDate());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }

    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder shortUrl = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(SECURE_RANDOM.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

      public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, start, end).stream()
                    .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
    
    //Analytics for total clicks by date for all URLs of a user
    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(urlMappings, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return clickEvents.stream()
                .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()));

    }

    public UrlMapping getOriginalUrl(String shortUrl) {
        String cachedUrl = redisTemplate.opsForValue().get("url:" + shortUrl);
        UrlMapping urlMapping;

        if (cachedUrl != null) {
            urlMapping = new UrlMapping();
            urlMapping.setOriginalUrl(cachedUrl);
            urlMapping.setShortUrl(shortUrl);
        } else {
            urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
            if (urlMapping != null) {
                redisTemplate.opsForValue().set("url:" + shortUrl, urlMapping.getOriginalUrl());
            } else {
                return null;
            }
        }

        // Process analytics asynchronously to avoid blocking the redirect response
        CompletableFuture.runAsync(() -> {
            UrlMapping mappingToUpdate = urlMappingRepository.findByShortUrl(shortUrl);
            if (mappingToUpdate != null) {
                mappingToUpdate.setClickCount(mappingToUpdate.getClickCount() + 1);
                urlMappingRepository.save(mappingToUpdate);

                ClickEvent clickEvent = new ClickEvent();
                clickEvent.setClickDate(LocalDateTime.now());
                clickEvent.setUrlMapping(mappingToUpdate);
                clickEventRepository.save(clickEvent);
            }
        });

        return urlMapping;
    }
}
