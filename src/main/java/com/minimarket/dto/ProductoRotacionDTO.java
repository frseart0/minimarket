package com.minimarket.dto;

/** Fila del reporte de rotación de productos: unidades vendidas totales por producto. */
public class ProductoRotacionDTO {

    private Long productoId;
    private String nombreProducto;
    private long cantidadVendida;

    public ProductoRotacionDTO() {
    }

    public ProductoRotacionDTO(Long productoId, String nombreProducto, long cantidadVendida) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }
}
