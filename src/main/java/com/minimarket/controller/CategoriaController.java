package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    private EntityModel<Categoria> toModel(Categoria categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"));
    }

    @Operation(summary = "Listar categorías", description = "Devuelve todas las categorías con enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public CollectionModel<EntityModel<Categoria>> listarCategorias() {
        List<EntityModel<Categoria>> categorias = categoriaService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(categorias,
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel());
    }

    @Operation(summary = "Obtener categoría por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoriaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(categoriaService.findById(id)));
    }

    @Operation(summary = "Crear categoría", description = "Registra una nueva categoría. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría creada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (se requiere ADMIN)")
    })
    @PostMapping
    public EntityModel<Categoria> guardarCategoria(@Valid @RequestBody Categoria categoria) {
        return toModel(categoriaService.save(categoria));
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
        categoriaService.findById(id);
        categoria.setId(id);
        return ResponseEntity.ok(toModel(categoriaService.save(categoria)));
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría por ID. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        categoriaService.findById(id);
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
