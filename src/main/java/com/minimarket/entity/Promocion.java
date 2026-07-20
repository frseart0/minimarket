package com.minimarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * Oferta o promoción especial gestionada de forma centralizada, asociada a
 * un producto y vigente entre {@code fechaInicio} y {@code fechaFin}.
 */
@Entity
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "El descuento debe ser mayor a 0%")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar el 100%")
    @Column(nullable = false)
    private Double porcentajeDescuento;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @Column(nullable = false)
    private Date fechaInicio;

    @NotNull
    @Column(nullable = false)
    private Date fechaFin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    /** @return true si la fecha dada cae dentro del rango de vigencia [fechaInicio, fechaFin]. */
    public boolean estaVigente(Date fecha) {
        return fechaInicio != null && fechaFin != null && fecha != null
                && !fecha.before(fechaInicio) && !fecha.after(fechaFin);
    }

    /** @return el precio del producto asociado luego de aplicar el descuento de esta promoción. */
    public double aplicarDescuento(double precioOriginal) {
        return precioOriginal * (1 - (porcentajeDescuento / 100.0));
    }
}
