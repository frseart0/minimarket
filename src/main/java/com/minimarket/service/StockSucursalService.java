package com.minimarket.service;

import com.minimarket.entity.StockSucursal;

import java.util.List;
import java.util.Optional;

public interface StockSucursalService {
    List<StockSucursal> findAll();
    StockSucursal findById(Long id);
    StockSucursal save(StockSucursal stockSucursal);
    void deleteById(Long id);

    /** Disponibilidad de un producto en todas las sucursales. */
    List<StockSucursal> findByProductoId(Long productoId);

    /** Stock de todos los productos en una sucursal específica. */
    List<StockSucursal> findBySucursalId(Long sucursalId);

    /** Registro puntual de stock de un producto en una sucursal, si existe. */
    Optional<StockSucursal> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

    /**
     * Ajusta (suma/resta) la cantidad en stock de un producto en una sucursal.
     * Si no existe un registro de stock para ese par producto/sucursal, se crea
     * uno con stock mínimo por defecto. Si tras el ajuste la cantidad cae al
     * nivel mínimo o por debajo, se dispara la generación automática de una
     * orden de compra de reposición.
     *
     * @param delta positivo para una entrada, negativo para una salida.
     */
    StockSucursal ajustarStock(Long productoId, Long sucursalId, int delta);
}
