package com.minimarket.service.impl;

import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Proveedor;
import com.minimarket.entity.Sucursal;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.OrdenCompraRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.ProveedorRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.OrdenCompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrdenCompraServiceImpl implements OrdenCompraService {

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Override
    public List<OrdenCompra> findAll() {
        return ordenCompraRepository.findAll();
    }

    @Override
    public OrdenCompra findById(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Orden de compra", id));
    }

    @Override
    public OrdenCompra save(OrdenCompra ordenCompra) {
        if (ordenCompra.getFecha() == null) {
            ordenCompra.setFecha(new Date());
        }
        return ordenCompraRepository.save(ordenCompra);
    }

    @Override
    public OrdenCompra cambiarEstado(Long id, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra orden = findById(id);
        orden.setEstado(nuevoEstado);
        return ordenCompraRepository.save(orden);
    }

    @Override
    public OrdenCompra generarAutomatica(Long productoId, Long sucursalId, int cantidadSolicitada) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Producto", productoId));
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Sucursal", sucursalId));
        Proveedor proveedor = obtenerProveedorPorDefecto();

        OrdenCompra orden = new OrdenCompra();
        orden.setProducto(producto);
        orden.setSucursal(sucursal);
        orden.setProveedor(proveedor);
        orden.setCantidadSolicitada(Math.max(cantidadSolicitada, 1));
        orden.setFecha(new Date());
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);
        orden.setAutomatica(true);

        return ordenCompraRepository.save(orden);
    }

    /** Usa el primer proveedor registrado o crea uno genérico si aún no existe ninguno. */
    private Proveedor obtenerProveedorPorDefecto() {
        return proveedorRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Proveedor generico = new Proveedor();
                    generico.setNombre("Proveedor genérico");
                    generico.setContacto("Por definir");
                    return proveedorRepository.save(generico);
                });
    }
}
