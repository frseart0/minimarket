package com.minimarket.service;

import com.minimarket.entity.EstadoPedido;
import com.minimarket.entity.Pedido;

import java.util.List;

public interface PedidoService {
    List<Pedido> findAll();
    Pedido findById(Long id);
    List<Pedido> findByUsuarioId(Long usuarioId);

    /**
     * Crea un pedido validando disponibilidad de stock en la sucursal,
     * aplicando promociones vigentes a cada línea y descontando el stock
     * correspondiente (registrando el movimiento de inventario asociado).
     */
    Pedido crearPedido(Pedido pedido);

    /**
     * Cambia el estado del pedido. Si se cancela un pedido que no estaba ya
     * entregado ni cancelado, repone el stock descontado originalmente.
     */
    Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado);
}
