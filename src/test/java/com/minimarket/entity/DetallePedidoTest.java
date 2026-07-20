package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DetallePedidoTest {

    @Test
    void calculaElSubtotalCorrectamente() {
        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(1000.0);

        assertEquals(3000.0, detalle.getSubtotal(), 0.001);
    }

    @Test
    void elSubtotalEsCeroConValoresNulosPorDefecto() {
        DetallePedido detalle = new DetallePedido();
        assertEquals(0.0, detalle.getSubtotal(), 0.001);
    }

    @Test
    void creaDetallePedidoConValoresValidos() {
        Producto producto = new Producto();
        producto.setNombre("Arroz");

        DetallePedido detalle = new DetallePedido();
        detalle.setId(1L);
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(1290.0);

        assertEquals(1L, detalle.getId());
        assertEquals("Arroz", detalle.getProducto().getNombre());
        assertEquals(2, detalle.getCantidad());
        assertEquals(1290.0, detalle.getPrecioUnitario());
        assertEquals(2580.0, detalle.getSubtotal(), 0.001);
    }
}
