package com.minimarket.service;

import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.EstadoPedido;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.TipoEntrega;
import com.minimarket.exception.BusinessRuleException;
import com.minimarket.repository.PedidoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link PedidoServiceImpl}: validación de stock,
 * aplicación de promociones vigentes, descuento de inventario y reposición
 * de stock al cancelar un pedido.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private StockSucursalService stockSucursalService;
    @Mock
    private PromocionService promocionService;
    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Producto producto;
    private Sucursal sucursal;
    private Pedido pedido;
    private DetallePedido detalle;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz 1kg");
        producto.setPrecio(1000.0);

        sucursal = new Sucursal();
        sucursal.setId(1L);

        detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(3);

        pedido = new Pedido();
        pedido.setSucursal(sucursal);
        pedido.setTipoEntrega(TipoEntrega.RETIRO_TIENDA);
        List<DetallePedido> detalles = new ArrayList<>();
        detalles.add(detalle);
        pedido.setDetalles(detalles);
    }

    @Test
    void crearPedidoRechazaSiFaltaElTipoDeEntrega() {
        pedido.setTipoEntrega(null);
        assertThrows(BusinessRuleException.class, () -> pedidoService.crearPedido(pedido));
    }

    @Test
    void crearPedidoRechazaSiNoTieneDetalles() {
        pedido.setDetalles(List.of());
        assertThrows(BusinessRuleException.class, () -> pedidoService.crearPedido(pedido));
    }

    @Test
    void crearPedidoRechazaSiNoHayStockRegistradoEnLaSucursal() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(stockSucursalService.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> pedidoService.crearPedido(pedido));
    }

    @Test
    void crearPedidoRechazaSiElStockEsInsuficiente() {
        StockSucursal stock = new StockSucursal();
        stock.setCantidad(1); // se pide 3, solo hay 1

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(stockSucursalService.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));

        assertThrows(BusinessRuleException.class, () -> pedidoService.crearPedido(pedido));
    }

    @Test
    void crearPedidoExitosoAplicaPromocionYDescuentaStock() {
        StockSucursal stock = new StockSucursal();
        stock.setCantidad(10);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(stockSucursalService.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));
        when(promocionService.calcularPrecioConDescuento(1L, 1000.0)).thenReturn(850.0);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Pedido guardado = pedidoService.crearPedido(pedido);

        assertEquals(EstadoPedido.PENDIENTE, guardado.getEstado());
        assertEquals(850.0, guardado.getDetalles().get(0).getPrecioUnitario());

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioService).save(captor.capture());
        assertEquals("SALIDA", captor.getValue().getTipoMovimiento());
        assertEquals(3, captor.getValue().getCantidad());
    }

    @Test
    void cambiarEstadoACanceladoReponeElStock() {
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        detalle.setPedido(pedido);
        detalle.setCantidad(2);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido actualizado = pedidoService.cambiarEstado(1L, EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, actualizado.getEstado());
        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioService).save(captor.capture());
        assertEquals("ENTRADA", captor.getValue().getTipoMovimiento());
    }

    @Test
    void cambiarEstadoDeEntregadoACanceladoNoReponeStock() {
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.ENTREGADO);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.cambiarEstado(1L, EstadoPedido.CANCELADO);

        verify(inventarioService, never()).save(any());
    }

    @Test
    void cambiarEstadoAConfirmadoNoAfectaElInventario() {
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido actualizado = pedidoService.cambiarEstado(1L, EstadoPedido.CONFIRMADO);

        assertEquals(EstadoPedido.CONFIRMADO, actualizado.getEstado());
        verify(inventarioService, never()).save(any());
    }
}
