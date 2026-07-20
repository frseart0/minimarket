package com.minimarket.service;

import com.minimarket.entity.Sucursal;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.impl.SucursalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceImplTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private SucursalServiceImpl sucursalService;

    @Test
    void findAllDevuelveTodasLasSucursales() {
        Sucursal s = new Sucursal();
        s.setId(1L);
        s.setNombre("Sucursal Centro");
        when(sucursalRepository.findAll()).thenReturn(List.of(s));

        assertEquals(1, sucursalService.findAll().size());
    }

    @Test
    void findByIdLanzaExcepcionCuandoNoExisteLaSucursal() {
        when(sucursalRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sucursalService.findById(5L));
    }

    @Test
    void findByIdDevuelveLaSucursalCuandoExiste() {
        Sucursal s = new Sucursal();
        s.setId(1L);
        s.setNombre("Sucursal Centro");
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));

        assertEquals("Sucursal Centro", sucursalService.findById(1L).getNombre());
    }

    @Test
    void saveDelegaEnElRepositorio() {
        Sucursal s = new Sucursal();
        when(sucursalRepository.save(s)).thenReturn(s);

        assertSame(s, sucursalService.save(s));
    }

    @Test
    void deleteByIdDelegaEnElRepositorio() {
        sucursalService.deleteById(1L);
        verify(sucursalRepository).deleteById(1L);
    }
}
