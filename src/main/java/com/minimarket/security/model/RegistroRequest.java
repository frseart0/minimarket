package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición de autoregistro de clientes ({@code POST /auth/registro}).
 * El usuario creado siempre recibe el rol CLIENTE; el registro de empleados
 * (CAJERO/ADMIN) se realiza mediante {@code POST /api/usuarios} (solo ADMIN).
 */
public class RegistroRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    public RegistroRequest() {
    }

    public RegistroRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
