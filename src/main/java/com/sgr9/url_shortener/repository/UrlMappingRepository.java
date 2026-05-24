package com.sgr9.url_shortener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgr9.url_shortener.models.UrlMapping;
import com.sgr9.url_shortener.models.User;

import java.util.List;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
   UrlMapping findByShortUrl(String shortUrl);
   List<UrlMapping> findByUser(User user);
}
