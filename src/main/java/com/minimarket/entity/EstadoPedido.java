package com.minimarket.entity;

/** Ciclo de vida de un pedido en línea. */
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO
}
