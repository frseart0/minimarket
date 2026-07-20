package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
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
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Registro y consulta de movimientos de stock")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    private EntityModel<Inventario> toModel(Inventario inventario) {
        return EntityModel.of(inventario,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(inventario.getId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario"));
    }

    @Operation(summary = "Listar movimientos de inventario",
            description = "Devuelve todos los movimientos de stock con enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (ADMIN o CAJERO)")
    })
    @GetMapping
    public CollectionModel<EntityModel<Inventario>> listarMovimientosDeInventario() {
        List<EntityModel<Inventario>> movimientos = inventarioService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(movimientos,
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel());
    }

    @Operation(summary = "Obtener movimiento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(inventarioService.findById(id)));
    }

    @Operation(summary = "Registrar movimiento de inventario",
            description = "Registra una entrada o salida de stock de un producto en una sucursal. "
                    + "Si la salida deja el stock de la sucursal en el nivel mínimo o por debajo, "
                    + "se genera automáticamente una orden de compra de reposición.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento registrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (ADMIN o CAJERO)")
    })
    @PostMapping
    public EntityModel<Inventario> registrarMovimiento(@Valid @RequestBody Inventario inventario) {
        if (inventario.getFechaMovimiento() == null) {
            inventario.setFechaMovimiento(new Date());
        }
        return toModel(inventarioService.save(inventario));
    }

    @Operation(summary = "Actualizar movimiento de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento actualizado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(@PathVariable Long id, @Valid @RequestBody Inventario inventario) {
        inventarioService.findById(id);
        inventario.setId(id);
        return ResponseEntity.ok(toModel(inventarioService.save(inventario)));
    }

    @Operation(summary = "Eliminar movimiento de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movimiento eliminado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        inventarioService.findById(id);
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
