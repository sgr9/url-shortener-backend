package com.sgr9.url_shortener.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sgr9.url_shortener.models.UrlMapping;
import com.sgr9.url_shortener.service.UrlMappingService;

@RestController
public class RedirectController {

    private final UrlMappingService urlMappingService;

    public RedirectController(UrlMappingService urlMappingService) {
        this.urlMappingService = urlMappingService;
    }

    // Redirect users who visit the bare backend URL to the frontend landing page
    @GetMapping("/")
    public ResponseEntity<Void> redirectToFrontend(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Location", "https://your-app.vercel.app"); // TODO: Replace with your actual Vercel URL
        return ResponseEntity.status(302).headers(httpHeaders).build();
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl){
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);
        if (urlMapping != null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(302).headers(httpHeaders).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
