package com.minimarket.security.model;

import java.util.List;

/** Respuesta de autenticación exitosa: token JWT y datos básicos del usuario. */
public class AuthResponse {

    private String token;
    private String tipo = "Bearer";
    private String username;
    private List<String> roles;
    private long expiraEnMs;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, List<String> roles, long expiraEnMs) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expiraEnMs = expiraEnMs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public long getExpiraEnMs() {
        return expiraEnMs;
    }

    public void setExpiraEnMs(long expiraEnMs) {
        this.expiraEnMs = expiraEnMs;
    }
}
