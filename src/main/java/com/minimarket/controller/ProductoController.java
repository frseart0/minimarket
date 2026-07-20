package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.service.ProductoService;
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

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión completa de productos del minimarket")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private StockSucursalService stockSucursalService;

    /** Construye la representación HATEOAS de un producto con sus enlaces. */
    private EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));
    }

    @Operation(summary = "Listar productos", description = "Devuelve todos los productos con enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public CollectionModel<EntityModel<Producto>> listarProductos() {
        List<EntityModel<Producto>> productos = productoService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
    }

    @Operation(summary = "Obtener producto por ID", description = "Devuelve un producto específico con sus enlaces.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(productoService.findById(id)));
    }

    @Operation(summary = "Consultar disponibilidad por sucursal",
            description = "Devuelve el stock del producto en cada sucursal, para que un cliente pueda "
                    + "verificar en qué tiendas hay unidades disponibles antes de retirar o pedir despacho.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}/disponibilidad")
    public CollectionModel<EntityModel<StockSucursal>> consultarDisponibilidad(@PathVariable Long id) {
        productoService.findById(id); // valida que el producto exista
        List<EntityModel<StockSucursal>> disponibilidad = stockSucursalService.findByProductoId(id).stream()
                .map(stock -> EntityModel.of(stock,
                        linkTo(methodOn(StockSucursalController.class).obtenerStockPorId(stock.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(disponibilidad,
                linkTo(methodOn(ProductoController.class).consultarDisponibilidad(id)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withRel("producto"));
    }

    @Operation(summary = "Crear producto", description = "Registra un nuevo producto. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto creado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (se requiere ADMIN)")
    })
    @PostMapping
    public EntityModel<Producto> guardarProducto(@Valid @RequestBody Producto producto) {
        return toModel(productoService.save(producto));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        productoService.findById(id); // lanza ResourceNotFoundException si no existe
        producto.setId(id);
        return ResponseEntity.ok(toModel(productoService.save(producto)));
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto por ID. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.findById(id); // lanza ResourceNotFoundException si no existe
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
