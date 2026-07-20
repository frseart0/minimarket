package com.minimarket;

import com.minimarket.security.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración del filtro {@code JwtAuthenticationFilter}: acceso
 * con token válido, ausencia de token, token malformado/con firma inválida y
 * token de un usuario que ya no existe. Complementa {@link AuthControllerTest}
 * (que cubre el endpoint de login) probando directamente el filtro sobre
 * recursos protegidos con distintos roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String tokenPara(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return jwtUtil.generarToken(new User(username, "irrelevante", authorities));
    }

    @Test
    void sinTokenElAccesoAUnRecursoProtegidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenMalformadoElAccesoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/sucursales").header("Authorization", "Bearer token-no-valido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenDeUnUsuarioInexistenteElAccesoDevuelve401() throws Exception {
        String token = tokenPara("usuario-que-no-existe", "ADMIN");
        mockMvc.perform(get("/api/sucursales").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenValidoDeAdminSePuedeListarSucursales() throws Exception {
        String token = tokenPara("admin", "ADMIN");
        mockMvc.perform(get("/api/sucursales").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void conTokenValidoDeClienteNoSePuedeAccederAReportes() throws Exception {
        String token = tokenPara("cliente", "CLIENTE");
        mockMvc.perform(get("/api/reportes/rotacion-productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void conTokenValidoDeAdminSePuedeAccederAReportes() throws Exception {
        String token = tokenPara("admin", "ADMIN");
        mockMvc.perform(get("/api/reportes/rotacion-productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void conTokenValidoDeCajeroSePuedenListarOrdenesDeCompra() throws Exception {
        String token = tokenPara("cajero", "CAJERO");
        mockMvc.perform(get("/api/ordenes-compra").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void conTokenValidoDeClienteNoSePuedeAccederAProveedores() throws Exception {
        String token = tokenPara("cliente", "CLIENTE");
        mockMvc.perform(get("/api/proveedores").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void conTokenValidoDeClienteSePuedeConsultarPromociones() throws Exception {
        String token = tokenPara("cliente", "CLIENTE");
        mockMvc.perform(get("/api/promociones").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void conTokenValidoDeClienteSePuedenConsultarSusPropiosPedidos() throws Exception {
        String token = tokenPara("cliente", "CLIENTE");
        mockMvc.perform(get("/api/pedidos/mis-pedidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void conTokenValidoDeClienteNoSePuedeListarTodosLosPedidos() throws Exception {
        String token = tokenPara("cliente", "CLIENTE");
        mockMvc.perform(get("/api/pedidos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
