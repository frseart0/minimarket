package com.minimarket.service.impl;

import com.minimarket.entity.Sucursal;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SucursalServiceImpl implements SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    @Override
    public List<Sucursal> findAll() {
        return sucursalRepository.findAll();
    }

    @Override
    public Sucursal findById(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Sucursal", id));
    }

    @Override
    public Sucursal save(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    @Override
    public void deleteById(Long id) {
        sucursalRepository.deleteById(id);
    }
}
