package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PromocionTest {

    private Promocion promocionVigenteDePrueba() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date ayer = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date enUnDia = calendar.getTime();

        Promocion promo = new Promocion();
        promo.setNombre("Descuento de prueba");
        promo.setPorcentajeDescuento(25.0);
        promo.setFechaInicio(ayer);
        promo.setFechaFin(enUnDia);
        return promo;
    }

    @Test
    void estaVigenteEsVerdaderoDentroDelRango() {
        Promocion promo = promocionVigenteDePrueba();
        assertTrue(promo.estaVigente(new Date()));
    }

    @Test
    void estaVigenteEsFalsoAntesDeLaFechaDeInicio() {
        Promocion promo = promocionVigenteDePrueba();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -10);

        assertFalse(promo.estaVigente(calendar.getTime()));
    }

    @Test
    void estaVigenteEsFalsoDespuesDeLaFechaDeFin() {
        Promocion promo = promocionVigenteDePrueba();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 10);

        assertFalse(promo.estaVigente(calendar.getTime()));
    }

    @Test
    void aplicarDescuentoCalculaElPrecioCorrectamente() {
        Promocion promo = new Promocion();
        promo.setPorcentajeDescuento(20.0);

        assertEquals(800.0, promo.aplicarDescuento(1000.0), 0.001);
    }

    @Test
    void aplicarDescuentoDeCeroPorCientoNoAlteraElPrecio() {
        Promocion promo = new Promocion();
        promo.setPorcentajeDescuento(0.0);

        assertEquals(1000.0, promo.aplicarDescuento(1000.0), 0.001);
    }
}
