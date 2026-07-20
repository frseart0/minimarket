package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Registro y consulta de ventas en tienda")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    private EntityModel<Venta> toModel(Venta venta) {
        return EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"));
    }

    @Operation(summary = "Listar ventas", description = "Devuelve todas las ventas registradas con enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public CollectionModel<EntityModel<Venta>> listarVentas() {
        List<EntityModel<Venta>> ventas = ventaService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(ventas,
                linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @Operation(summary = "Obtener venta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Venta>> obtenerVentaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(ventaService.findById(id)));
    }

    @Operation(summary = "Registrar venta", description = "Registra una nueva venta. Requiere rol ADMIN o CAJERO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta registrada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (ADMIN o CAJERO)")
    })
    @PostMapping
    public EntityModel<Venta> guardarVenta(@Valid @RequestBody Venta venta) {
        if (venta.getFecha() == null) {
            venta.setFecha(new Date());
        }
        return toModel(ventaService.save(venta));
    }

    @Operation(summary = "Actualizar venta", description = "Actualiza una venta existente. Requiere rol ADMIN o CAJERO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta actualizada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Venta>> actualizarVenta(@PathVariable Long id, @Valid @RequestBody Venta venta) {
        ventaService.findById(id);
        venta.setId(id);
        return ResponseEntity.ok(toModel(ventaService.save(venta)));
    }

    @Operation(summary = "Eliminar venta", description = "Elimina una venta por ID. Requiere rol ADMIN o CAJERO.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Venta eliminada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.findById(id);
        ventaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
