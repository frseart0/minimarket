package com.minimarket.controller;

import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.service.OrdenCompraService;
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

/**
 * Órdenes de compra a proveedores, ya sea creadas manualmente o generadas
 * automáticamente cuando el stock de una sucursal alcanza el nivel mínimo.
 */
@RestController
@RequestMapping("/api/ordenes-compra")
@Tag(name = "Órdenes de compra", description = "Reposición de stock (manual o generada automáticamente)")
public class OrdenCompraController {

    @Autowired
    private OrdenCompraService ordenCompraService;

    private EntityModel<OrdenCompra> toModel(OrdenCompra orden) {
        return EntityModel.of(orden,
                linkTo(methodOn(OrdenCompraController.class).obtenerOrdenPorId(orden.getId())).withSelfRel(),
                linkTo(methodOn(OrdenCompraController.class).listarOrdenes()).withRel("ordenes-compra"));
    }

    @Operation(summary = "Listar órdenes de compra", description = "Incluye tanto las creadas manualmente como las generadas automáticamente.")
    @GetMapping
    public CollectionModel<EntityModel<OrdenCompra>> listarOrdenes() {
        List<EntityModel<OrdenCompra>> ordenes = ordenCompraService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(ordenes,
                linkTo(methodOn(OrdenCompraController.class).listarOrdenes()).withSelfRel());
    }

    @Operation(summary = "Obtener orden de compra por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<OrdenCompra>> obtenerOrdenPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(ordenCompraService.findById(id)));
    }

    @Operation(summary = "Crear orden de compra manual", description = "Requiere rol ADMIN o CAJERO.")
    @PostMapping
    public EntityModel<OrdenCompra> guardarOrden(@Valid @RequestBody OrdenCompra ordenCompra) {
        return toModel(ordenCompraService.save(ordenCompra));
    }

    @Operation(summary = "Cambiar estado de una orden de compra",
            description = "Transiciona la orden a RECIBIDA o CANCELADA. Requiere rol ADMIN o CAJERO.")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntityModel<OrdenCompra>> cambiarEstado(@PathVariable Long id, @RequestParam EstadoOrdenCompra estado) {
        return ResponseEntity.ok(toModel(ordenCompraService.cambiarEstado(id, estado)));
    }
}
