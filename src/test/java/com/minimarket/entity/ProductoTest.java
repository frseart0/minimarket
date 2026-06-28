package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Producto: escenarios de éxito y de borde.
 */
class ProductoTest {

    @Test
    void creaProductoConValoresValidos() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Bebidas");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua mineral");
        producto.setPrecio(990.0);
        producto.setStock(50);
        producto.setCategoria(categoria);

        assertEquals(1L, producto.getId());
        assertEquals("Agua mineral", producto.getNombre());
        assertEquals(990.0, producto.getPrecio());
        assertEquals(50, producto.getStock());
        assertEquals("Bebidas", producto.getCategoria().getNombre());
    }

    @Test
    void productoNuevoTieneCamposNulosPorDefecto() {
        Producto producto = new Producto();
        assertNull(producto.getId());
        assertNull(producto.getNombre());
        assertNull(producto.getPrecio());
        assertNull(producto.getStock());
        assertNull(producto.getCategoria());
    }

    @Test
    void permiteStockEnCeroComoCasoLimite() {
        Producto producto = new Producto();
        producto.setStock(0);
        producto.setPrecio(0.0);
        assertEquals(0, producto.getStock());
        assertEquals(0.0, producto.getPrecio());
    }
}
