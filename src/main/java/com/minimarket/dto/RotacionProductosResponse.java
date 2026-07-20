package com.minimarket.dto;

import java.util.List;

/** Respuesta del reporte de rotación: productos más y menos vendidos en el mismo listado. */
public class RotacionProductosResponse {

    private List<ProductoRotacionDTO> masVendidos;
    private List<ProductoRotacionDTO> menosVendidos;

    public RotacionProductosResponse() {
    }

    public RotacionProductosResponse(List<ProductoRotacionDTO> masVendidos, List<ProductoRotacionDTO> menosVendidos) {
        this.masVendidos = masVendidos;
        this.menosVendidos = menosVendidos;
    }

    public List<ProductoRotacionDTO> getMasVendidos() {
        return masVendidos;
    }

    public void setMasVendidos(List<ProductoRotacionDTO> masVendidos) {
        this.masVendidos = masVendidos;
    }

    public List<ProductoRotacionDTO> getMenosVendidos() {
        return menosVendidos;
    }

    public void setMenosVendidos(List<ProductoRotacionDTO> menosVendidos) {
        this.menosVendidos = menosVendidos;
    }
}
