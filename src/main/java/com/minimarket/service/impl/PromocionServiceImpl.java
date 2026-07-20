package com.minimarket.service.impl;

import com.minimarket.entity.Promocion;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.service.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PromocionServiceImpl implements PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    @Override
    public List<Promocion> findAll() {
        return promocionRepository.findAll();
    }

    @Override
    public Promocion findById(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Promoción", id));
    }

    @Override
    public Promocion save(Promocion promocion) {
        return promocionRepository.save(promocion);
    }

    @Override
    public void deleteById(Long id) {
        promocionRepository.deleteById(id);
    }

    @Override
    public Optional<Promocion> findVigentePorProducto(Long productoId) {
        Date ahora = new Date();
        return promocionRepository.findByProductoId(productoId).stream()
                .filter(promo -> promo.estaVigente(ahora))
                .findFirst();
    }

    @Override
    public double calcularPrecioConDescuento(Long productoId, double precioOriginal) {
        return findVigentePorProducto(productoId)
                .map(promo -> promo.aplicarDescuento(precioOriginal))
                .orElse(precioOriginal);
    }
}
