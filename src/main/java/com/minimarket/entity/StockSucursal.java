package com.minimarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Stock de un {@link Producto} específico en una {@link Sucursal} específica.
 * Es la fuente de verdad para la gestión de inventario centralizado: permite
 * consultar disponibilidad por tienda y disparar reposición automática cuando
 * {@code cantidad <= stockMinimo}.
 */
@Entity
@Table(name = "stock_sucursal", uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "sucursal_id"}))
public class StockSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @NotNull
    @PositiveOrZero(message = "La cantidad en stock no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false)
    private Integer stockMinimo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    /** @return true si la cantidad actual llegó o cayó bajo el umbral mínimo. */
    public boolean requiereReposicion() {
        return cantidad != null && stockMinimo != null && cantidad <= stockMinimo;
    }
}
