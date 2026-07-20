package com.minimarket.controller;

import com.minimarket.entity.EstadoPedido;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.service.PedidoService;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Pedidos en línea con retiro en tienda o despacho a domicilio. Un CLIENTE
 * solo puede crear/consultar sus propios pedidos; ADMIN y CAJERO gestionan
 * el ciclo de vida completo (ver reglas en {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Pedidos en línea (retiro en tienda / despacho a domicilio)")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    private EntityModel<Pedido> toModel(Pedido pedido) {
        return EntityModel.of(pedido,
                linkTo(methodOn(PedidoController.class).obtenerPedidoPorId(pedido.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listarPedidos()).withRel("pedidos"));
    }

    @Operation(summary = "Crear pedido en línea",
            description = "El CLIENTE crea el pedido a su propio nombre; ADMIN/CAJERO pueden registrar "
                    + "pedidos telefónicos indicando el usuario. Valida stock por sucursal y aplica promociones vigentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente")
    })
    @PostMapping
    public EntityModel<Pedido> crearPedido(@Valid @RequestBody Pedido pedido, Authentication authentication) {
        boolean esStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ADMIN") || rol.equals("CAJERO"));

        if (!esStaff || pedido.getUsuario() == null) {
            Usuario usuarioAutenticado = usuarioService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
            pedido.setUsuario(usuarioAutenticado);
        }

        return toModel(pedidoService.crearPedido(pedido));
    }

    @Operation(summary = "Listar todos los pedidos", description = "Requiere rol ADMIN o CAJERO.")
    @GetMapping
    public CollectionModel<EntityModel<Pedido>> listarPedidos() {
        List<EntityModel<Pedido>> pedidos = pedidoService.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).listarPedidos()).withSelfRel());
    }

    @Operation(summary = "Listar mis pedidos", description = "Devuelve los pedidos del usuario autenticado.")
    @GetMapping("/mis-pedidos")
    public CollectionModel<EntityModel<Pedido>> listarMisPedidos(Authentication authentication) {
        Usuario usuarioAutenticado = usuarioService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        List<EntityModel<Pedido>> pedidos = pedidoService.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).listarMisPedidos(null)).withSelfRel());
    }

    @Operation(summary = "Obtener pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> obtenerPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(pedidoService.findById(id)));
    }

    @Operation(summary = "Cambiar estado del pedido",
            description = "Transiciona el pedido (CONFIRMADO, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO). "
                    + "Al cancelar un pedido no entregado, se repone el stock descontado. Requiere rol ADMIN o CAJERO.")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntityModel<Pedido>> cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return ResponseEntity.ok(toModel(pedidoService.cambiarEstado(id, estado)));
    }
}
