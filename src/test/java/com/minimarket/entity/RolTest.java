package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Rol y sus constructores.
 */
class RolTest {

    @Test
    void creaRolConConstructorDeNombre() {
        Rol rol = new Rol("ADMIN");
        assertEquals("ADMIN", rol.getNombre());
        assertNull(rol.getId());
    }

    @Test
    void creaRolConConstructorCompleto() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        Rol rol = new Rol(1L, "CAJERO", Set.of(usuario));

        assertEquals(1L, rol.getId());
        assertEquals("CAJERO", rol.getNombre());
        assertEquals(1, rol.getUsuarios().size());
    }

    @Test
    void permiteModificarNombreDeRol() {
        Rol rol = new Rol("CLIENTE");
        rol.setNombre("CLIENTE_PREMIUM");
        assertEquals("CLIENTE_PREMIUM", rol.getNombre());
    }
}
