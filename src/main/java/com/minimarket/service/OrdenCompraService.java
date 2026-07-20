package com.minimarket.service;

import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.OrdenCompra;

import java.util.List;

public interface OrdenCompraService {
    List<OrdenCompra> findAll();
    OrdenCompra findById(Long id);
    OrdenCompra save(OrdenCompra ordenCompra);
    OrdenCompra cambiarEstado(Long id, EstadoOrdenCompra nuevoEstado);

    /**
     * Genera automáticamente una orden de compra PENDIENTE para reponer un
     * producto en una sucursal, usando el primer proveedor registrado
     * (o creando uno genérico si no hay ninguno). Se invoca cuando el stock
     * de la sucursal alcanza o cae bajo el nivel mínimo.
     */
    OrdenCompra generarAutomatica(Long productoId, Long sucursalId, int cantidadSolicitada);
}
