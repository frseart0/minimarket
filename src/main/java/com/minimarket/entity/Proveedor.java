package com.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Proveedor externo al que se generan las órdenes de compra de reposición. */
@Entity
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Column(nullable = false)
    private String nombre;

    private String contacto;

    private String telefono;

    private String email;

    @JsonIgnore
    @OneToMany(mappedBy = "proveedor")
    private List<OrdenCompra> ordenes;

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

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrdenCompra> getOrdenes() {
        return ordenes;
    }

    public void setOrdenes(List<OrdenCompra> ordenes) {
        this.ordenes = ordenes;
    }
}
