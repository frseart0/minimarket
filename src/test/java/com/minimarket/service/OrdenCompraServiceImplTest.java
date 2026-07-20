package com.minimarket.service;

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
import com.minimarket.service.impl.OrdenCompraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceImplTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private OrdenCompraServiceImpl ordenCompraService;

    private Producto producto;
    private Sucursal sucursal;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        sucursal = new Sucursal();
        sucursal.setId(1L);
        proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Proveedor de prueba");
    }

    @Test
    void findByIdLanzaExcepcionCuandoNoExiste() {
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ordenCompraService.findById(1L));
    }

    @Test
    void generarAutomaticaUsaElPrimerProveedorExistente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(proveedorRepository.findAll()).thenReturn(List.of(proveedor));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra orden = ordenCompraService.generarAutomatica(1L, 1L, 15);

        assertEquals(EstadoOrdenCompra.PENDIENTE, orden.getEstado());
        assertTrue(orden.isAutomatica());
        assertEquals(15, orden.getCantidadSolicitada());
        assertSame(proveedor, orden.getProveedor());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void generarAutomaticaCreaUnProveedorGenericoSiNoHayNinguno() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(proveedorRepository.findAll()).thenReturn(Collections.emptyList());
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra orden = ordenCompraService.generarAutomatica(1L, 1L, 10);

        assertNotNull(orden.getProveedor());
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    void generarAutomaticaExigeAlMenosUnaUnidad() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(proveedorRepository.findAll()).thenReturn(List.of(proveedor));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra orden = ordenCompraService.generarAutomatica(1L, 1L, 0);

        assertEquals(1, orden.getCantidadSolicitada());
    }

    @Test
    void cambiarEstadoActualizaYPersisteLaOrden() {
        OrdenCompra existente = new OrdenCompra();
        existente.setId(5L);
        existente.setEstado(EstadoOrdenCompra.PENDIENTE);
        when(ordenCompraRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra actualizada = ordenCompraService.cambiarEstado(5L, EstadoOrdenCompra.RECIBIDA);

        assertEquals(EstadoOrdenCompra.RECIBIDA, actualizada.getEstado());
    }
}
