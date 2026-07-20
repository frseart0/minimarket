package com.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Sucursal de "MiniMarket Plus". El stock de cada producto se gestiona de
 * forma independiente por sucursal a través de {@link StockSucursal}.
 */
@Entity
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotBlank(message = "La dirección de la sucursal es obligatoria")
    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String comuna;

    private String telefono;

    @JsonIgnore
    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockSucursal> stock;

    // Getters y Setters
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<StockSucursal> getStock() {
        return stock;
    }

    public void setStock(List<StockSucursal> stock) {
        this.stock = stock;
    }
}
