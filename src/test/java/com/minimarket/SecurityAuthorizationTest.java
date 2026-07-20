package com.minimarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de autenticación y control de acceso (RBAC) del backend
 * "Minimarket Plus". Verifican que cada endpoint clave respete los roles
 * CLIENTE, CAJERO y ADMINISTRADOR (ADMIN).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- Acceso público ----------
    @Test
    void endpointPublicoEsAccesibleSinAutenticacion() throws Exception {
        mockMvc.perform(get("/public/hola"))
                .andExpect(status().isOk());
    }

    @Test
    void recursoProtegidoSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Productos ----------
    @Test
    @WithMockUser(username = "cliente", authorities = {"CLIENTE"})
    void clientePuedeConsultarProductos() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "cliente", authorities = {"CLIENTE"})
    void clienteNoPuedeEliminarProductos() throws Exception {
        mockMvc.perform(delete("/api/productos/9999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void adminPuedeAdministrarProductos() throws Exception {
        // 404 = la autorización pasó (el producto inexistente no se encuentra)
        mockMvc.perform(delete("/api/productos/9999"))
                .andExpect(status().isNotFound());
    }

    // ---------- Inventario ----------
    @Test
    @WithMockUser(username = "cliente", authorities = {"CLIENTE"})
    void clienteNoPuedeAccederInventario() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "cajero", authorities = {"CAJERO"})
    void cajeroPuedeConsultarInventario() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    // ---------- Ventas ----------
    @Test
    @WithMockUser(username = "cliente", authorities = {"CLIENTE"})
    void clienteNoPuedeGenerarVentas() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "cajero", authorities = {"CAJERO"})
    void cajeroEstaAutorizadoAGenerarVentas() throws Exception {
        // El cajero SÍ supera el control de acceso: la petición alcanza el controlador.
        // Con un cuerpo incompleto (sin usuario) el GlobalExceptionHandler devuelve un
        // error de negocio/servidor (no 403), lo que confirma que la autorización fue concedida.
        mockMvc.perform(post("/api/ventas")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> assertFalse(
                        result.getResponse().getStatus() == 403,
                        "El cajero no debe ser bloqueado por control de acceso"));
    }

    // ---------- Usuarios ----------
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void adminPuedeGestionarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "cajero", authorities = {"CAJERO"})
    void cajeroNoPuedeGestionarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }
}
