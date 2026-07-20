package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
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
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Operaciones para agregar y eliminar productos del carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    private EntityModel<Carrito> toModel(Carrito carrito) {
        return EntityModel.of(carrito,
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carrito"));
    }

    @Operation(summary = "Listar carrito", description = "Devuelve los productos en el carrito con enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public CollectionModel<EntityModel<Carrito>> listarCarrito() {
        List<EntityModel<Carrito>> items = carritoService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(items,
                linkTo(methodOn(CarritoController.class).listarCarrito()).withSelfRel());
    }

    @Operation(summary = "Obtener ítem del carrito por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ítem encontrado"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Carrito>> obtenerCarritoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(carritoService.findById(id)));
    }

    @Operation(summary = "Agregar producto al carrito",
            description = "Añade un producto al carrito validando disponibilidad de stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto agregado al carrito"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    public EntityModel<Carrito> agregarProductoAlCarrito(@Valid @RequestBody Carrito carrito) {
        return toModel(carritoService.save(carrito));
    }

    @Operation(summary = "Actualizar ítem del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ítem actualizado"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(@PathVariable Long id, @Valid @RequestBody Carrito carrito) {
        carritoService.findById(id);
        carrito.setId(id);
        return ResponseEntity.ok(toModel(carritoService.save(carrito)));
    }

    @Operation(summary = "Eliminar producto del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado del carrito"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        carritoService.findById(id);
        carritoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
