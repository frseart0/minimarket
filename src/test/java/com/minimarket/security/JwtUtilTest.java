package com.minimarket.security;

import com.minimarket.security.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de {@link JwtUtil}: generación, extracción de claims y
 * validación de tokens (casos de éxito, token de otro usuario y token expirado).
 */
class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("clave-secreta-de-pruebas-minimarket-plus-2026", 3600000L);

    private UserDetails usuario(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new User(username, "password", authorities);
    }

    @Test
    void generaUnTokenNoNuloYConTresSegmentos() {
        String token = jwtUtil.generarToken(usuario("admin", "ADMIN"));
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extraeElUsernameCorrectoDelToken() {
        String token = jwtUtil.generarToken(usuario("cajero", "CAJERO"));
        assertEquals("cajero", jwtUtil.extraerUsername(token));
    }

    @Test
    void extraeLosRolesCorrectosDelToken() {
        String token = jwtUtil.generarToken(usuario("admin", "ADMIN"));
        List<String> roles = jwtUtil.extraerRoles(token);
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    void elTokenEsValidoParaElMismoUsuario() {
        UserDetails admin = usuario("admin", "ADMIN");
        String token = jwtUtil.generarToken(admin);
        assertTrue(jwtUtil.esTokenValido(token, admin));
    }

    @Test
    void elTokenNoEsValidoParaOtroUsuario() {
        String token = jwtUtil.generarToken(usuario("admin", "ADMIN"));
        UserDetails otro = usuario("cliente", "CLIENTE");
        assertFalse(jwtUtil.esTokenValido(token, otro));
    }

    @Test
    void unTokenExpiradoNoEsValido() throws InterruptedException {
        JwtUtil jwtDeExpiracionCorta = new JwtUtil("clave-secreta-de-pruebas-minimarket-plus-2026", 1L);
        UserDetails admin = usuario("admin", "ADMIN");
        String token = jwtDeExpiracionCorta.generarToken(admin);
        Thread.sleep(15);
        assertFalse(jwtDeExpiracionCorta.esTokenValido(token, admin));
    }

    @Test
    void unTokenManipuladoNoEsValido() {
        String token = jwtUtil.generarToken(usuario("admin", "ADMIN"));
        String tokenManipulado = token.substring(0, token.length() - 2) + "xx";
        UserDetails admin = usuario("admin", "ADMIN");
        assertFalse(jwtUtil.esTokenValido(tokenManipulado, admin));
    }
}
