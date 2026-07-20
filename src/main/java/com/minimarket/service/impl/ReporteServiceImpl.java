package com.minimarket.service.impl;

import com.minimarket.dto.ProductoRotacionDTO;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Genera el reporte de rotación de productos (más y menos vendidos) a partir de las ventas registradas. */
@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Override
    public List<ProductoRotacionDTO> productosMasVendidos(int top) {
        List<ProductoRotacionDTO> ranking = obtenerRankingCompleto();
        ranking.sort(Comparator.comparingLong(ProductoRotacionDTO::getCantidadVendida).reversed());
        return limitar(ranking, top);
    }

    @Override
    public List<ProductoRotacionDTO> productosMenosVendidos(int top) {
        List<ProductoRotacionDTO> ranking = obtenerRankingCompleto();
        ranking.sort(Comparator.comparingLong(ProductoRotacionDTO::getCantidadVendida));
        return limitar(ranking, top);
    }

    private List<ProductoRotacionDTO> obtenerRankingCompleto() {
        return detalleVentaRepository.agregarCantidadVendidaPorProducto().stream()
                .map(fila -> new ProductoRotacionDTO((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .collect(Collectors.toList());
    }

    private List<ProductoRotacionDTO> limitar(List<ProductoRotacionDTO> ranking, int top) {
        if (ranking.isEmpty()) {
            return Collections.emptyList();
        }
        int limite = Math.max(1, Math.min(top, ranking.size()));
        return ranking.subList(0, limite);
    }
}
