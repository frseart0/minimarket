package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Inventario: movimientos de entrada/salida
 * y su asociación obligatoria a un Producto.
 */
class InventarioTest {

    @Test
    void registraMovimientoDeEntrada() {
        Producto producto = new Producto();
        producto.setNombre("Arroz");

        Date ahora = new Date();
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(100);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(ahora);

        assertEquals(1L, inventario.getId());
        assertEquals("Arroz", inventario.getProducto().getNombre());
        assertEquals(100, inventario.getCantidad());
        assertEquals("Entrada", inventario.getTipoMovimiento());
        assertEquals(ahora, inventario.getFechaMovimiento());
    }

    @Test
    void registraMovimientoDeSalida() {
        Inventario inventario = new Inventario();
        inventario.setTipoMovimiento("Salida");
        inventario.setCantidad(5);
        assertEquals("Salida", inventario.getTipoMovimiento());
        assertEquals(5, inventario.getCantidad());
    }

    @Test
    void cantidadEnCeroEsUnCasoLimite() {
        Inventario inventario = new Inventario();
        inventario.setCantidad(0);
        assertEquals(0, inventario.getCantidad());
    }
}
