package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.StockSucursal;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.StockSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private StockSucursalService stockSucursalService;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Movimiento de inventario", id));
    }

    /**
     * Registra el movimiento y aplica su efecto sobre el stock centralizado de la
     * sucursal correspondiente ({@link StockSucursal}). Si tras el movimiento el
     * stock cae al nivel mínimo o por debajo, se genera automáticamente una
     * orden de compra de reposición (ver {@link StockSucursalService}).
     */
    @Override
    public Inventario save(Inventario inventario) {
        Inventario guardado = inventarioRepository.save(inventario);
        boolean esEntrada = "ENTRADA".equalsIgnoreCase(guardado.getTipoMovimiento());
        int delta = esEntrada ? guardado.getCantidad() : -guardado.getCantidad();
        stockSucursalService.ajustarStock(
                guardado.getProducto().getId(),
                guardado.getSucursal().getId(),
                delta);
        return guardado;
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }
}
