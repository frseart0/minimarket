package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockSucursalTest {

    @Test
    void creaStockSucursalConValoresValidos() {
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("Sucursal Centro");

        StockSucursal stock = new StockSucursal();
        stock.setId(1L);
        stock.setProducto(producto);
        stock.setSucursal(sucursal);
        stock.setCantidad(20);
        stock.setStockMinimo(5);

        assertEquals(1L, stock.getId());
        assertEquals("Arroz", stock.getProducto().getNombre());
        assertEquals("Sucursal Centro", stock.getSucursal().getNombre());
        assertEquals(20, stock.getCantidad());
        assertEquals(5, stock.getStockMinimo());
    }

    @Test
    void requiereReposicionEsFalsoCuandoLaCantidadSuperaElMinimo() {
        StockSucursal stock = new StockSucursal();
        stock.setCantidad(10);
        stock.setStockMinimo(5);
        assertFalse(stock.requiereReposicion());
    }

    @Test
    void requiereReposicionEsVerdaderoCuandoLaCantidadIgualaElMinimo() {
        StockSucursal stock = new StockSucursal();
        stock.setCantidad(5);
        stock.setStockMinimo(5);
        assertTrue(stock.requiereReposicion());
    }

    @Test
    void requiereReposicionEsVerdaderoCuandoLaCantidadEstaBajoElMinimo() {
        StockSucursal stock = new StockSucursal();
        stock.setCantidad(2);
        stock.setStockMinimo(5);
        assertTrue(stock.requiereReposicion());
    }

    @Test
    void requiereReposicionEsFalsoConValoresNulos() {
        StockSucursal stock = new StockSucursal();
        assertFalse(stock.requiereReposicion());
    }
}
