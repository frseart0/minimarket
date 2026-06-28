package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad DetalleVenta: cantidades, precios y subtotal.
 */
class DetalleVentaTest {

    @Test
    void creaDetalleConProductoCantidadYPrecio() {
        Producto producto = new Producto();
        producto.setNombre("Galletas");

        Venta venta = new Venta();
        venta.setId(10L);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(4);
        detalle.setPrecio(1200.0);

        assertEquals(1L, detalle.getId());
        assertEquals(10L, detalle.getVenta().getId());
        assertEquals("Galletas", detalle.getProducto().getNombre());
        assertEquals(4, detalle.getCantidad());
        assertEquals(1200.0, detalle.getPrecio());
    }

    @Test
    void calculaSubtotalApartirDeCantidadYPrecio() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(3);
        detalle.setPrecio(500.0);

        double subtotal = detalle.getCantidad() * detalle.getPrecio();
        assertEquals(1500.0, subtotal);
    }

    @Test
    void precioCeroEsCasoLimite() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(1);
        detalle.setPrecio(0.0);
        assertEquals(0.0, detalle.getCantidad() * detalle.getPrecio());
    }
}
