package com.tiendalibros.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Genera y valida tokens JWT firmados con HS256.
 */
@Service
public class JwtService {

    private static final String CLAIM_ROL = "rol";

    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {

        // HS256 exige una clave de al menos 256 bits (32 bytes)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_ROL, rol)
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Lanza JwtException si el token es inválido, está manipulado o expiró. */
    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get(CLAIM_ROL, String.class);
    }

    public boolean esTokenValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
