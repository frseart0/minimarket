package com.minimarket.service.impl;

import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.EstadoPedido;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.exception.BusinessRuleException;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.PedidoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.PedidoService;
import com.minimarket.service.PromocionService;
import com.minimarket.service.StockSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Set<EstadoPedido> ESTADOS_SIN_REPOSICION_AL_CANCELAR =
            EnumSet.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockSucursalService stockSucursalService;

    @Autowired
    private PromocionService promocionService;

    @Autowired
    private InventarioService inventarioService;

    @Override
    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Pedido", id));
    }

    @Override
    public List<Pedido> findByUsuarioId(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Pedido crearPedido(Pedido pedido) {
        if (pedido.getTipoEntrega() == null) {
            throw new BusinessRuleException("El tipo de entrega (RETIRO_TIENDA o DESPACHO_DOMICILIO) es obligatorio");
        }
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new BusinessRuleException("El pedido debe incluir al menos un producto");
        }
        if (pedido.getSucursal() == null || pedido.getSucursal().getId() == null) {
            throw new BusinessRuleException("Debe indicar la sucursal donde se realizará el pedido");
        }

        Long sucursalId = pedido.getSucursal().getId();

        for (DetallePedido detalle : pedido.getDetalles()) {
            Long productoId = detalle.getProducto().getId();
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Producto", productoId));

            StockSucursal stock = stockSucursalService.findByProductoIdAndSucursalId(productoId, sucursalId)
                    .orElseThrow(() -> new BusinessRuleException(
                            "El producto '" + producto.getNombre() + "' no tiene stock registrado en la sucursal seleccionada"));

            if (stock.getCantidad() < detalle.getCantidad()) {
                throw new BusinessRuleException(
                        "Stock insuficiente para '" + producto.getNombre() + "' en la sucursal seleccionada "
                                + "(disponible: " + stock.getCantidad() + ", solicitado: " + detalle.getCantidad() + ")");
            }

            double precioConDescuento = promocionService.calcularPrecioConDescuento(productoId, producto.getPrecio());
            detalle.setProducto(producto);
            detalle.setPrecioUnitario(precioConDescuento);
            detalle.setPedido(pedido);
        }

        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFecha(new Date());
        Pedido guardado = pedidoRepository.save(pedido);

        // Descuenta el stock reservando los productos del pedido (movimiento de salida por sucursal).
        for (DetallePedido detalle : guardado.getDetalles()) {
            registrarMovimientoInventario(detalle, sucursalId, "SALIDA");
        }

        return guardado;
    }

    @Override
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = findById(id);
        EstadoPedido estadoAnterior = pedido.getEstado();

        if (nuevoEstado == EstadoPedido.CANCELADO && !ESTADOS_SIN_REPOSICION_AL_CANCELAR.contains(estadoAnterior)) {
            Long sucursalId = pedido.getSucursal().getId();
            for (DetallePedido detalle : pedido.getDetalles()) {
                registrarMovimientoInventario(detalle, sucursalId, "ENTRADA");
            }
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    private void registrarMovimientoInventario(DetallePedido detalle, Long sucursalId, String tipoMovimiento) {
        Sucursal sucursalRef = new Sucursal();
        sucursalRef.setId(sucursalId);

        Inventario movimiento = new Inventario();
        movimiento.setProducto(detalle.getProducto());
        movimiento.setSucursal(sucursalRef);
        movimiento.setCantidad(detalle.getCantidad());
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setFechaMovimiento(new Date());
        inventarioService.save(movimiento);
    }
}
