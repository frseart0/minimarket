package com.minimarket.controller;

import com.minimarket.entity.Promocion;
import com.minimarket.service.PromocionService;
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
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Ofertas y promociones especiales gestionadas de forma centralizada")
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    private EntityModel<Promocion> toModel(Promocion promocion) {
        return EntityModel.of(promocion,
                linkTo(methodOn(PromocionController.class).obtenerPromocionPorId(promocion.getId())).withSelfRel(),
                linkTo(methodOn(PromocionController.class).listarPromociones()).withRel("promociones"));
    }

    @Operation(summary = "Listar promociones")
    @GetMapping
    public CollectionModel<EntityModel<Promocion>> listarPromociones() {
        List<EntityModel<Promocion>> promociones = promocionService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(promociones,
                linkTo(methodOn(PromocionController.class).listarPromociones()).withSelfRel());
    }

    @Operation(summary = "Obtener promoción por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción encontrada"),
            @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Promocion>> obtenerPromocionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(promocionService.findById(id)));
    }

    @Operation(summary = "Crear promoción", description = "Requiere rol ADMIN.")
    @PostMapping
    public EntityModel<Promocion> guardarPromocion(@Valid @RequestBody Promocion promocion) {
        return toModel(promocionService.save(promocion));
    }

    @Operation(summary = "Actualizar promoción", description = "Requiere rol ADMIN.")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Promocion>> actualizarPromocion(@PathVariable Long id, @Valid @RequestBody Promocion promocion) {
        promocionService.findById(id);
        promocion.setId(id);
        return ResponseEntity.ok(toModel(promocionService.save(promocion)));
    }

    @Operation(summary = "Eliminar promoción", description = "Requiere rol ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPromocion(@PathVariable Long id) {
        promocionService.findById(id);
        promocionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
