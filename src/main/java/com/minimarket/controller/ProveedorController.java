package com.minimarket.controller;

import com.minimarket.entity.Proveedor;
import com.minimarket.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Proveedores utilizados en las órdenes de compra de reposición")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    private EntityModel<Proveedor> toModel(Proveedor proveedor) {
        return EntityModel.of(proveedor,
                linkTo(methodOn(ProveedorController.class).obtenerProveedorPorId(proveedor.getId())).withSelfRel(),
                linkTo(methodOn(ProveedorController.class).listarProveedores()).withRel("proveedores"));
    }

    @Operation(summary = "Listar proveedores")
    @GetMapping
    public CollectionModel<EntityModel<Proveedor>> listarProveedores() {
        List<EntityModel<Proveedor>> proveedores = proveedorService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(proveedores,
                linkTo(methodOn(ProveedorController.class).listarProveedores()).withSelfRel());
    }

    @Operation(summary = "Obtener proveedor por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Proveedor>> obtenerProveedorPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(proveedorService.findById(id)));
    }

    @Operation(summary = "Crear proveedor", description = "Requiere rol ADMIN.")
    @PostMapping
    public EntityModel<Proveedor> guardarProveedor(@Valid @RequestBody Proveedor proveedor) {
        return toModel(proveedorService.save(proveedor));
    }

    @Operation(summary = "Actualizar proveedor", description = "Requiere rol ADMIN.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Proveedor>> actualizarProveedor(@PathVariable Long id, @Valid @RequestBody Proveedor proveedor) {
        proveedorService.findById(id);
        proveedor.setId(id);
        return ResponseEntity.ok(toModel(proveedorService.save(proveedor)));
    }

    @Operation(summary = "Eliminar proveedor", description = "Requiere rol ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        proveedorService.findById(id);
        proveedorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
