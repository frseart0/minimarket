package com.minimarket.service;

import com.minimarket.dto.ProductoRotacionDTO;

import java.util.List;

public interface ReporteService {
    /** Los {@code top} productos más vendidos (mayor cantidad primero). */
    List<ProductoRotacionDTO> productosMasVendidos(int top);

    /** Los {@code top} productos menos vendidos (menor cantidad primero). */
    List<ProductoRotacionDTO> productosMenosVendidos(int top);
}
