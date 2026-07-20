package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    void creaPedidoConValoresValidos() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("Sucursal Centro");
        Date fecha = new Date();

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuario(usuario);
        pedido.setSucursal(sucursal);
        pedido.setTipoEntrega(TipoEntrega.DESPACHO_DOMICILIO);
        pedido.setDireccionEntrega("Calle Falsa 123");
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFecha(fecha);
        pedido.setDetalles(List.of());

        assertEquals(1L, pedido.getId());
        assertEquals("cliente1", pedido.getUsuario().getUsername());
        assertEquals(TipoEntrega.DESPACHO_DOMICILIO, pedido.getTipoEntrega());
        assertEquals("Calle Falsa 123", pedido.getDireccionEntrega());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(fecha, pedido.getFecha());
        assertTrue(pedido.getDetalles().isEmpty());
    }

    @Test
    void pedidoNuevoTieneEstadoPendientePorDefecto() {
        Pedido pedido = new Pedido();
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    @Test
    void pedidoDeRetiroEnTiendaPuedeNoTenerDireccionDeEntrega() {
        Pedido pedido = new Pedido();
        pedido.setTipoEntrega(TipoEntrega.RETIRO_TIENDA);
        assertNull(pedido.getDireccionEntrega());
    }
}
