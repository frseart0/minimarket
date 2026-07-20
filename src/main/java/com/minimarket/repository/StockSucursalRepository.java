package com.minimarket.repository;

import com.minimarket.entity.StockSucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockSucursalRepository extends JpaRepository<StockSucursal, Long> {
    List<StockSucursal> findByProductoId(Long productoId);
    List<StockSucursal> findBySucursalId(Long sucursalId);
    Optional<StockSucursal> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}
