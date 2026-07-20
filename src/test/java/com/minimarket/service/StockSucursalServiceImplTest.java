package com.minimarket.service;

import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.impl.StockSucursalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link StockSucursalServiceImpl}: ajuste de stock
 * (entradas/salidas) y disparo de la reposición automática cuando la
 * cantidad alcanza o cae bajo el stock mínimo.
 */
@ExtendWith(MockitoExtension.class)
class StockSucursalServiceImplTest {

    @Mock
    private StockSucursalRepository stockSucursalRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private OrdenCompraService ordenCompraService;

    @InjectMocks
    private StockSucursalServiceImpl stockSucursalService;

    private Producto producto;
    private Sucursal sucursal;
    private StockSucursal stock;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        sucursal = new Sucursal();
        sucursal.setId(1L);

        stock = new StockSucursal();
        stock.setId(10L);
        stock.setProducto(producto);
        stock.setSucursal(sucursal);
        stock.setCantidad(10);
        stock.setStockMinimo(5);
    }

    @Test
    void ajustarStockConEntradaAumentaLaCantidad() {
        when(stockSucursalRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = stockSucursalService.ajustarStock(1L, 1L, 5);

        assertEquals(15, resultado.getCantidad());
        verify(ordenCompraService, never()).generarAutomatica(any(), any(), anyInt());
    }

    @Test
    void ajustarStockConSalidaQueNoLlegaAlMinimoNoDisparaReposicion() {
        when(stockSucursalRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = stockSucursalService.ajustarStock(1L, 1L, -3);

        assertEquals(7, resultado.getCantidad());
        verify(ordenCompraService, never()).generarAutomatica(any(), any(), anyInt());
    }

    @Test
    void ajustarStockQueLlegaAlMinimoDisparaReposicionAutomatica() {
        when(stockSucursalRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = stockSucursalService.ajustarStock(1L, 1L, -5);

        assertEquals(5, resultado.getCantidad());
        assertTrue(resultado.requiereReposicion());
        verify(ordenCompraService).generarAutomatica(eq(1L), eq(1L), anyInt());
    }

    @Test
    void ajustarStockQueCaeBajoElMinimoDisparaReposicionAutomatica() {
        when(stockSucursalRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        stockSucursalService.ajustarStock(1L, 1L, -8);

        verify(ordenCompraService, times(1)).generarAutomatica(eq(1L), eq(1L), anyInt());
    }

    @Test
    void ajustarStockNuncaDejaLaCantidadNegativa() {
        when(stockSucursalRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = stockSucursalService.ajustarStock(1L, 1L, -100);

        assertEquals(0, resultado.getCantidad());
    }

    @Test
    void ajustarStockCreaElRegistroSiNoExisteAunConStockMinimoPorDefecto() {
        when(stockSucursalRepository.findByProductoIdAndSucursalId(2L, 1L)).thenReturn(Optional.empty());
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));

        ArgumentCaptor<StockSucursal> captor = ArgumentCaptor.forClass(StockSucursal.class);
        when(stockSucursalRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = stockSucursalService.ajustarStock(2L, 1L, 20);

        assertEquals(20, resultado.getCantidad());
        assertEquals(5, captor.getValue().getStockMinimo());
    }
}
