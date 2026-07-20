package com.minimarket.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilidad para generar y validar JSON Web Tokens (JWT) firmados con HMAC-SHA256.
 * El token incluye el nombre de usuario (subject) y sus roles (claim "roles"),
 * lo que permite reconstruir la autenticación sin consultar la base de datos
 * en cada request (autenticación sin estado / stateless).
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration-ms}") long expirationMs) {
        // La clave configurada (jwt.secret) se usa como texto plano (bytes UTF-8).
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            // HS256 requiere una clave de al menos 256 bits; se rellena de forma determinística.
            keyBytes = (secret + secret).getBytes();
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /** Genera un token JWT para el usuario autenticado, incluyendo sus roles como claim. */
    public String generarToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    public String extraerUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        return (List<String>) parseClaims(token).get("roles", List.class);
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();
            return username != null
                    && username.equals(userDetails.getUsername())
                    && !estaExpirado(claims);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean estaExpirado(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            // Se propaga con los claims igualmente accesibles para quien necesite el detalle,
            // pero para la validación general se trata como token inválido.
            throw ex;
        }
    }
}
