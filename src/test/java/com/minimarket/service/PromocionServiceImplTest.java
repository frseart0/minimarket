package com.minimarket.service;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.service.impl.PromocionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceImplTest {

    @Mock
    private PromocionRepository promocionRepository;

    @InjectMocks
    private PromocionServiceImpl promocionService;

    private Promocion promocionVigente(Long productoId, double descuento) {
        Producto producto = new Producto();
        producto.setId(productoId);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date ayer = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date enDiezDias = calendar.getTime();

        Promocion promo = new Promocion();
        promo.setProducto(producto);
        promo.setPorcentajeDescuento(descuento);
        promo.setFechaInicio(ayer);
        promo.setFechaFin(enDiezDias);
        return promo;
    }

    private Promocion promocionVencida(Long productoId) {
        Producto producto = new Producto();
        producto.setId(productoId);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date haceUnMes = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date haceVeinteDias = calendar.getTime();

        Promocion promo = new Promocion();
        promo.setProducto(producto);
        promo.setPorcentajeDescuento(20.0);
        promo.setFechaInicio(haceUnMes);
        promo.setFechaFin(haceVeinteDias);
        return promo;
    }

    @Test
    void findVigentePorProductoDevuelvePromocionActiva() {
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(promocionVigente(1L, 10.0)));

        Optional<Promocion> resultado = promocionService.findVigentePorProducto(1L);

        assertTrue(resultado.isPresent());
    }

    @Test
    void findVigentePorProductoIgnoraPromocionesVencidas() {
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(promocionVencida(1L)));

        Optional<Promocion> resultado = promocionService.findVigentePorProducto(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void calcularPrecioConDescuentoAplicaElPorcentajeVigente() {
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(promocionVigente(1L, 20.0)));

        double precioFinal = promocionService.calcularPrecioConDescuento(1L, 1000.0);

        assertEquals(800.0, precioFinal, 0.001);
    }

    @Test
    void calcularPrecioConDescuentoDevuelvePrecioOriginalSinPromocionVigente() {
        when(promocionRepository.findByProductoId(2L)).thenReturn(List.of());

        double precioFinal = promocionService.calcularPrecioConDescuento(2L, 500.0);

        assertEquals(500.0, precioFinal, 0.001);
    }
}
