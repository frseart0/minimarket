package com.minimarket.controller;

import com.minimarket.entity.StockSucursal;
import com.minimarket.service.StockSucursalService;
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

/** Gestión y consulta del stock centralizado de productos por sucursal. */
@RestController
@RequestMapping("/api/stock-sucursal")
@Tag(name = "Stock por sucursal", description = "Inventario centralizado: stock y stock mínimo por producto/sucursal")
public class StockSucursalController {

    @Autowired
    private StockSucursalService stockSucursalService;

    private EntityModel<StockSucursal> toModel(StockSucursal stock) {
        return EntityModel.of(stock,
                linkTo(methodOn(StockSucursalController.class).obtenerStockPorId(stock.getId())).withSelfRel(),
                linkTo(methodOn(StockSucursalController.class).listarStock()).withRel("stock-sucursal"));
    }

    @Operation(summary = "Listar todo el stock por sucursal")
    @GetMapping
    public CollectionModel<EntityModel<StockSucursal>> listarStock() {
        List<EntityModel<StockSucursal>> items = stockSucursalService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(items,
                linkTo(methodOn(StockSucursalController.class).listarStock()).withSelfRel());
    }

    @Operation(summary = "Obtener registro de stock por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<StockSucursal>> obtenerStockPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(stockSucursalService.findById(id)));
    }

    @Operation(summary = "Listar stock de una sucursal específica")
    @GetMapping("/sucursal/{sucursalId}")
    public CollectionModel<EntityModel<StockSucursal>> listarPorSucursal(@PathVariable Long sucursalId) {
        List<EntityModel<StockSucursal>> items = stockSucursalService.findBySucursalId(sucursalId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(items,
                linkTo(methodOn(StockSucursalController.class).listarPorSucursal(sucursalId)).withSelfRel());
    }

    @Operation(summary = "Crear registro de stock", description = "Define el stock inicial y el stock mínimo de un producto en una sucursal.")
    @PostMapping
    public EntityModel<StockSucursal> guardarStock(@Valid @RequestBody StockSucursal stockSucursal) {
        return toModel(stockSucursalService.save(stockSucursal));
    }

    @Operation(summary = "Actualizar registro de stock", description = "Permite ajustar manualmente la cantidad o el stock mínimo.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<StockSucursal>> actualizarStock(@PathVariable Long id, @Valid @RequestBody StockSucursal stockSucursal) {
        stockSucursalService.findById(id);
        stockSucursal.setId(id);
        return ResponseEntity.ok(toModel(stockSucursalService.save(stockSucursal)));
    }

    @Operation(summary = "Eliminar registro de stock")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarStock(@PathVariable Long id) {
        stockSucursalService.findById(id);
        stockSucursalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
