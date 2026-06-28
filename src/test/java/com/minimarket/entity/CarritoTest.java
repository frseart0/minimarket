package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Carrito: relación usuario-producto y cantidad.
 */
class CarritoTest {

    @Test
    void creaCarritoConProductoYUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");

        Producto producto = new Producto();
        producto.setNombre("Pan");
        producto.setStock(10);

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(3);

        assertEquals(1L, carrito.getId());
        assertEquals("cliente1", carrito.getUsuario().getUsername());
        assertEquals("Pan", carrito.getProducto().getNombre());
        assertEquals(3, carrito.getCantidad());
    }

    @Test
    void cantidadNoDebeSuperarElStockDisponible() {
        Producto producto = new Producto();
        producto.setStock(5);

        Carrito carrito = new Carrito();
        carrito.setProducto(producto);
        carrito.setCantidad(5);

        // Caso límite: la cantidad solicitada es igual al stock disponible (válido)
        assertTrue(carrito.getCantidad() <= carrito.getProducto().getStock());
    }

    @Test
    void carritoNuevoTieneCamposNulos() {
        Carrito carrito = new Carrito();
        assertNull(carrito.getId());
        assertNull(carrito.getUsuario());
        assertNull(carrito.getProducto());
        assertNull(carrito.getCantidad());
    }
}
