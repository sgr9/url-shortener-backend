package com.sgr9.url_shortener.security.jwt;


import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String token;
}