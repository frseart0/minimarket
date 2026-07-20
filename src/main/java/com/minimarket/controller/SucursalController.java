package com.minimarket.controller;

import com.minimarket.entity.Sucursal;
import com.minimarket.service.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/sucursales")
@Tag(name = "Sucursales", description = "Gestión de las sucursales de MiniMarket Plus")
public class SucursalController {

    @Autowired
    private SucursalService sucursalService;

    private EntityModel<Sucursal> toModel(Sucursal sucursal) {
        return EntityModel.of(sucursal,
                linkTo(methodOn(SucursalController.class).obtenerSucursalPorId(sucursal.getId())).withSelfRel(),
                linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("sucursales"),
                linkTo(methodOn(StockSucursalController.class).listarPorSucursal(sucursal.getId())).withRel("stock"));
    }

    @Operation(summary = "Listar sucursales")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"))
    @GetMapping
    public CollectionModel<EntityModel<Sucursal>> listarSucursales() {
        List<EntityModel<Sucursal>> sucursales = sucursalService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(sucursales,
                linkTo(methodOn(SucursalController.class).listarSucursales()).withSelfRel());
    }

    @Operation(summary = "Obtener sucursal por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucursal encontrada"),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Sucursal>> obtenerSucursalPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(sucursalService.findById(id)));
    }

    @Operation(summary = "Crear sucursal", description = "Registra una nueva sucursal. Requiere rol ADMIN.")
    @PostMapping
    public EntityModel<Sucursal> guardarSucursal(@Valid @RequestBody Sucursal sucursal) {
        return toModel(sucursalService.save(sucursal));
    }

    @Operation(summary = "Actualizar sucursal", description = "Requiere rol ADMIN.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Sucursal>> actualizarSucursal(@PathVariable Long id, @Valid @RequestBody Sucursal sucursal) {
        sucursalService.findById(id);
        sucursal.setId(id);
        return ResponseEntity.ok(toModel(sucursalService.save(sucursal)));
    }

    @Operation(summary = "Eliminar sucursal", description = "Requiere rol ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSucursal(@PathVariable Long id) {
        sucursalService.findById(id);
        sucursalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
