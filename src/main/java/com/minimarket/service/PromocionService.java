package com.minimarket.service;

import com.minimarket.entity.Promocion;

import java.util.List;
import java.util.Optional;

public interface PromocionService {
    List<Promocion> findAll();
    Promocion findById(Long id);
    Promocion save(Promocion promocion);
    void deleteById(Long id);

    /** Promoción vigente hoy para un producto, si existe alguna. */
    Optional<Promocion> findVigentePorProducto(Long productoId);

    /** Precio final de un producto aplicando su promoción vigente (si tiene). */
    double calcularPrecioConDescuento(Long productoId, double precioOriginal);
}
