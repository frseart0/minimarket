package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Venta y su detalle de productos.
 */
class VentaTest {

    @Test
    void creaVentaConDetalles() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cajero1");

        Producto producto = new Producto();
        producto.setNombre("Café");

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecio(2500.0);

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(List.of(detalle));

        assertEquals(1L, venta.getId());
        assertEquals("cajero1", venta.getUsuario().getUsername());
        assertNotNull(venta.getFecha());
        assertEquals(1, venta.getDetalles().size());
        assertEquals(2, venta.getDetalles().get(0).getCantidad());
    }

    @Test
    void ventaSinDetallesEsCasoLimite() {
        Venta venta = new Venta();
        venta.setDetalles(List.of());
        assertTrue(venta.getDetalles().isEmpty());
    }

    @Test
    void ventaNuevaTieneCamposNulos() {
        Venta venta = new Venta();
        assertNull(venta.getId());
        assertNull(venta.getUsuario());
        assertNull(venta.getFecha());
        assertNull(venta.getDetalles());
    }
}
