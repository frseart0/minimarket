package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.OrdenCompraService;
import com.minimarket.service.StockSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Stock mínimo asignado por defecto a un producto nuevo en una sucursal. */
@Service
public class StockSucursalServiceImpl implements StockSucursalService {

    private static final int STOCK_MINIMO_POR_DEFECTO = 5;

    @Autowired
    private StockSucursalRepository stockSucursalRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Override
    public List<StockSucursal> findAll() {
        return stockSucursalRepository.findAll();
    }

    @Override
    public StockSucursal findById(Long id) {
        return stockSucursalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Stock de sucursal", id));
    }

    @Override
    public StockSucursal save(StockSucursal stockSucursal) {
        return stockSucursalRepository.save(stockSucursal);
    }

    @Override
    public void deleteById(Long id) {
        stockSucursalRepository.deleteById(id);
    }

    @Override
    public List<StockSucursal> findByProductoId(Long productoId) {
        return stockSucursalRepository.findByProductoId(productoId);
    }

    @Override
    public List<StockSucursal> findBySucursalId(Long sucursalId) {
        return stockSucursalRepository.findBySucursalId(sucursalId);
    }

    @Override
    public Optional<StockSucursal> findByProductoIdAndSucursalId(Long productoId, Long sucursalId) {
        return stockSucursalRepository.findByProductoIdAndSucursalId(productoId, sucursalId);
    }

    @Override
    public StockSucursal ajustarStock(Long productoId, Long sucursalId, int delta) {
        StockSucursal stock = stockSucursalRepository.findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseGet(() -> crearRegistroStock(productoId, sucursalId));

        int nuevaCantidad = Math.max(0, stock.getCantidad() + delta);
        stock.setCantidad(nuevaCantidad);
        StockSucursal actualizado = stockSucursalRepository.save(stock);

        if (actualizado.requiereReposicion()) {
            int cantidadAReponer = Math.max(actualizado.getStockMinimo() * 3, 10);
            ordenCompraService.generarAutomatica(productoId, sucursalId, cantidadAReponer);
        }

        return actualizado;
    }

    private StockSucursal crearRegistroStock(Long productoId, Long sucursalId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Producto", productoId));
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Sucursal", sucursalId));

        StockSucursal nuevo = new StockSucursal();
        nuevo.setProducto(producto);
        nuevo.setSucursal(sucursal);
        nuevo.setCantidad(0);
        nuevo.setStockMinimo(STOCK_MINIMO_POR_DEFECTO);
        return nuevo;
    }
}
