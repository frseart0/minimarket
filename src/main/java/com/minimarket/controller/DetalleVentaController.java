package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
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
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle de ventas", description = "Líneas de detalle de cada venta (producto, cantidad, precio)")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    private EntityModel<DetalleVenta> toModel(DetalleVenta detalleVenta) {
        return EntityModel.of(detalleVenta,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalleVenta.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"));
    }

    @Operation(summary = "Listar detalles de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (ADMIN o CAJERO)")
    })
    @GetMapping
    public CollectionModel<EntityModel<DetalleVenta>> listarDetalleVentas() {
        List<EntityModel<DetalleVenta>> detalles = detalleVentaService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(detalles,
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @Operation(summary = "Obtener detalle de venta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DetalleVenta>> obtenerDetalleVentaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(detalleVentaService.findById(id)));
    }

    @Operation(summary = "Registrar detalle de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle registrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (ADMIN o CAJERO)")
    })
    @PostMapping
    public EntityModel<DetalleVenta> guardarDetalleVenta(@Valid @RequestBody DetalleVenta detalleVenta) {
        return toModel(detalleVentaService.save(detalleVenta));
    }

    @Operation(summary = "Actualizar detalle de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle actualizado"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DetalleVenta>> actualizarDetalleVenta(@PathVariable Long id, @Valid @RequestBody DetalleVenta detalleVenta) {
        detalleVentaService.findById(id);
        detalleVenta.setId(id);
        return ResponseEntity.ok(toModel(detalleVentaService.save(detalleVenta)));
    }

    @Operation(summary = "Eliminar detalle de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Detalle eliminado"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        detalleVentaService.findById(id);
        detalleVentaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
