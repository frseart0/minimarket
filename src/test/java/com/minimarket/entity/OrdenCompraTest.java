package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class OrdenCompraTest {

    @Test
    void creaOrdenCompraConValoresValidos() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Distribuidora Central");
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("Sucursal Centro");
        Date fecha = new Date();

        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setProveedor(proveedor);
        orden.setProducto(producto);
        orden.setSucursal(sucursal);
        orden.setCantidadSolicitada(20);
        orden.setFecha(fecha);
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);
        orden.setAutomatica(true);

        assertEquals(1L, orden.getId());
        assertEquals("Distribuidora Central", orden.getProveedor().getNombre());
        assertEquals(20, orden.getCantidadSolicitada());
        assertEquals(EstadoOrdenCompra.PENDIENTE, orden.getEstado());
        assertTrue(orden.isAutomatica());
    }

    @Test
    void ordenCompraNuevaTieneEstadoPendienteYNoAutomaticaPorDefecto() {
        OrdenCompra orden = new OrdenCompra();
        assertEquals(EstadoOrdenCompra.PENDIENTE, orden.getEstado());
        assertFalse(orden.isAutomatica());
    }
}
