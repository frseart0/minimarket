package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Categoria y su relación con Producto.
 */
class CategoriaTest {

    @Test
    void creaCategoriaConProductosAsociados() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        Producto leche = new Producto();
        leche.setNombre("Leche entera");
        categoria.setProductos(List.of(leche));

        assertEquals(1L, categoria.getId());
        assertEquals("Lácteos", categoria.getNombre());
        assertEquals(1, categoria.getProductos().size());
        assertEquals("Leche entera", categoria.getProductos().get(0).getNombre());
    }

    @Test
    void categoriaSinProductosEsCasoLimiteValido() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Vacía");
        categoria.setProductos(List.of());
        assertTrue(categoria.getProductos().isEmpty());
    }

    @Test
    void categoriaNuevaTieneCamposNulos() {
        Categoria categoria = new Categoria();
        assertNull(categoria.getId());
        assertNull(categoria.getNombre());
        assertNull(categoria.getProductos());
    }
}
