package com.minimarket.controller;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.security.model.AuthResponse;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.model.RegistroRequest;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.RolService;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controlador de autenticación: login (emisión de JWT) y autoregistro de
 * clientes. Ambos endpoints son de acceso público (ver {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Login y registro de usuarios (emisión de JWT)")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario (cliente, cajero o administrador) y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, token JWT emitido"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<EntityModel<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generarToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        AuthResponse body = new AuthResponse(token, userDetails.getUsername(), roles, jwtUtil.getExpirationMs());
        EntityModel<AuthResponse> modelo = EntityModel.of(body,
                linkTo(methodOn(AuthController.class).login(loginRequest)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));

        return ResponseEntity.ok(modelo);
    }

    @Operation(summary = "Registrar cliente", description = "Autoregistro público de un nuevo usuario con rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "409", description = "El nombre de usuario ya está en uso")
    })
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest registroRequest) {
        if (usuarioService.findByUsername(registroRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso");
        }

        Rol rolCliente = rolService.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("El rol CLIENTE no está inicializado"));

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(registroRequest.getUsername());
        // La contraseña en texto plano es cifrada por UsuarioServiceImpl#save antes de persistir.
        nuevoUsuario.setPassword(registroRequest.getPassword());
        nuevoUsuario.setRoles(Set.of(rolCliente));

        Usuario guardado = usuarioService.save(nuevoUsuario);

        EntityModel<Usuario> modelo = EntityModel.of(guardado,
                linkTo(methodOn(AuthController.class).login(null)).withRel("login"));

        return ResponseEntity.status(HttpStatus.CREATED).body(modelo);
    }
}
