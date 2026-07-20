package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SucursalTest {

    @Test
    void creaSucursalConValoresValidos() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Calle Falsa 123");
        sucursal.setComuna("Santiago");
        sucursal.setTelefono("+56 2 1234 5678");

        assertEquals(1L, sucursal.getId());
        assertEquals("Sucursal Centro", sucursal.getNombre());
        assertEquals("Calle Falsa 123", sucursal.getDireccion());
        assertEquals("Santiago", sucursal.getComuna());
        assertEquals("+56 2 1234 5678", sucursal.getTelefono());
    }

    @Test
    void sucursalNuevaTieneCamposNulosPorDefecto() {
        Sucursal sucursal = new Sucursal();
        assertNull(sucursal.getId());
        assertNull(sucursal.getNombre());
        assertNull(sucursal.getStock());
    }
}
