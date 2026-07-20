package com.minimarket.service;

import com.minimarket.entity.Proveedor;

import java.util.List;

public interface ProveedorService {
    List<Proveedor> findAll();
    Proveedor findById(Long id);
    Proveedor save(Proveedor proveedor);
    void deleteById(Long id);
}
