package com.minimarket.controller;

import com.minimarket.dto.RotacionProductosResponse;
import com.minimarket.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Reportes de gestión: rotación de productos (más y menos vendidos). Acceso exclusivo ADMIN. */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Reportes de rotación de productos para la gestión de inventario")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Operation(summary = "Reporte de rotación de productos",
            description = "Devuelve los productos más y menos vendidos, según las unidades vendidas registradas en las ventas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte generado correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (se requiere ADMIN)")
    })
    @GetMapping("/rotacion-productos")
    public EntityModel<RotacionProductosResponse> rotacionDeProductos(@RequestParam(defaultValue = "5") int top) {
        RotacionProductosResponse reporte = new RotacionProductosResponse(
                reporteService.productosMasVendidos(top),
                reporteService.productosMenosVendidos(top));

        return EntityModel.of(reporte,
                linkTo(methodOn(ReporteController.class).rotacionDeProductos(top)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));
    }
}
