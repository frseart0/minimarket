package com.minimarket.service;

import com.minimarket.dto.ProductoRotacionDTO;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.service.impl.ReporteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Pruebas unitarias de {@link ReporteServiceImpl}: ranking de productos más y menos vendidos. */
@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    private List<Object[]> filasDeEjemplo() {
        return List.of(
                new Object[]{1L, "Agua mineral", 50L},
                new Object[]{2L, "Bebida cola", 30L},
                new Object[]{3L, "Arroz", 5L}
        );
    }

    @Test
    void productosMasVendidosOrdenaDeMayorAMenor() {
        when(detalleVentaRepository.agregarCantidadVendidaPorProducto()).thenReturn(filasDeEjemplo());

        List<ProductoRotacionDTO> resultado = reporteService.productosMasVendidos(2);

        assertEquals(2, resultado.size());
        assertEquals("Agua mineral", resultado.get(0).getNombreProducto());
        assertEquals(50L, resultado.get(0).getCantidadVendida());
        assertEquals("Bebida cola", resultado.get(1).getNombreProducto());
    }

    @Test
    void productosMenosVendidosOrdenaDeMenorAMayor() {
        when(detalleVentaRepository.agregarCantidadVendidaPorProducto()).thenReturn(filasDeEjemplo());

        List<ProductoRotacionDTO> resultado = reporteService.productosMenosVendidos(2);

        assertEquals(2, resultado.size());
        assertEquals("Arroz", resultado.get(0).getNombreProducto());
        assertEquals(5L, resultado.get(0).getCantidadVendida());
    }

    @Test
    void reporteConTopMayorQueElTotalDevuelveTodosLosProductos() {
        when(detalleVentaRepository.agregarCantidadVendidaPorProducto()).thenReturn(filasDeEjemplo());

        List<ProductoRotacionDTO> resultado = reporteService.productosMasVendidos(100);

        assertEquals(3, resultado.size());
    }

    @Test
    void reporteSinVentasDevuelveListaVacia() {
        when(detalleVentaRepository.agregarCantidadVendidaPorProducto()).thenReturn(Collections.emptyList());

        assertTrue(reporteService.productosMasVendidos(5).isEmpty());
        assertTrue(reporteService.productosMenosVendidos(5).isEmpty());
    }
}
