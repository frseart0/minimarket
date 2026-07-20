package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProveedorTest {

    @Test
    void creaProveedorConValoresValidos() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Distribuidora Central");
        proveedor.setContacto("Juan Pérez");
        proveedor.setTelefono("+56 2 2345 6789");
        proveedor.setEmail("contacto@distribuidora.cl");

        assertEquals(1L, proveedor.getId());
        assertEquals("Distribuidora Central", proveedor.getNombre());
        assertEquals("Juan Pérez", proveedor.getContacto());
        assertEquals("+56 2 2345 6789", proveedor.getTelefono());
        assertEquals("contacto@distribuidora.cl", proveedor.getEmail());
    }

    @Test
    void proveedorNuevoTieneCamposNulosPorDefecto() {
        Proveedor proveedor = new Proveedor();
        assertNull(proveedor.getId());
        assertNull(proveedor.getNombre());
        assertNull(proveedor.getOrdenes());
    }
}
