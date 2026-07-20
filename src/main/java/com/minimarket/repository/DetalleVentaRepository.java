package com.minimarket.repository;

import com.minimarket.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVentaId(Long ventaId);

    /**
     * Agrega las unidades vendidas por producto para el reporte de rotación
     * (productos más y menos vendidos). Cada fila: [productoId, nombreProducto, totalVendido].
     */
    @Query("SELECT dv.producto.id, dv.producto.nombre, SUM(dv.cantidad) " +
            "FROM DetalleVenta dv GROUP BY dv.producto.id, dv.producto.nombre " +
            "ORDER BY SUM(dv.cantidad) DESC")
    List<Object[]> agregarCantidadVendidaPorProducto();
}
