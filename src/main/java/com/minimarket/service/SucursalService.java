package com.minimarket.service;

import com.minimarket.entity.Sucursal;

import java.util.List;

public interface SucursalService {
    List<Sucursal> findAll();
    Sucursal findById(Long id);
    Sucursal save(Sucursal sucursal);
    void deleteById(Long id);
}
