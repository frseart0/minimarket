package com.minimarket.service;

import com.minimarket.entity.Producto;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de {@link ProductoServiceImpl} con el repositorio mockeado. */
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua mineral");
        producto.setPrecio(990.0);
        producto.setStock(50);
    }

    @Test
    void findAllDevuelveTodosLosProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.findAll();

        assertEquals(1, resultado.size());
        verify(productoRepository).findAll();
    }

    @Test
    void findByIdDevuelveElProductoCuandoExiste() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.findById(1L);

        assertEquals("Agua mineral", resultado.getNombre());
    }

    @Test
    void findByIdLanzaExcepcionCuandoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productoService.findById(99L));
    }

    @Test
    void saveDelegaEnElRepositorio() {
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.save(producto);

        assertSame(producto, resultado);
        verify(productoRepository).save(producto);
    }

    @Test
    void deleteByIdDelegaEnElRepositorio() {
        productoService.deleteById(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void findByCategoriaIdDelegaEnElRepositorio() {
        when(productoRepository.findByCategoriaId(2L)).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.findByCategoriaId(2L);

        assertEquals(1, resultado.size());
    }
}
