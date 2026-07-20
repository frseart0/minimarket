package com.minimarket.security.config;

import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad basada en JWT (autenticación sin estado) y
 * autorización por roles (RBAC): ADMIN, CAJERO y CLIENTE.
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                           JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        // Sin JWT o token invalido en un recurso protegido -> 401 (en vez del 403 por defecto)
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Acceso público: autenticación y documentación
                        .requestMatchers("/public/**", "/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/h2-console/**"
                        ).permitAll()

                        // Gestión de usuarios: solo ADMIN
                        .requestMatchers("/api/usuarios/**").hasAuthority("ADMIN")

                        // Productos: lectura autenticada, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                        .requestMatchers("/api/productos/**").hasAuthority("ADMIN")

                        // Categorías: lectura autenticada, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").authenticated()
                        .requestMatchers("/api/categorias/**").hasAuthority("ADMIN")

                        // Sucursales: lectura autenticada, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/sucursales/**").authenticated()
                        .requestMatchers("/api/sucursales/**").hasAuthority("ADMIN")

                        // Stock por sucursal: consulta autenticada, gestión ADMIN/CAJERO
                        .requestMatchers(HttpMethod.GET, "/api/stock-sucursal/**").authenticated()
                        .requestMatchers("/api/stock-sucursal/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Inventario (movimientos de stock): ADMIN y CAJERO
                        .requestMatchers("/api/inventario/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Proveedores: solo ADMIN
                        .requestMatchers("/api/proveedores/**").hasAuthority("ADMIN")

                        // Órdenes de compra (incluye las generadas automáticamente): ADMIN y CAJERO
                        .requestMatchers("/api/ordenes-compra/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Reportes (rotación de productos): solo ADMIN
                        .requestMatchers("/api/reportes/**").hasAuthority("ADMIN")

                        // Promociones: lectura autenticada, gestión solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/promociones/**").authenticated()
                        .requestMatchers("/api/promociones/**").hasAuthority("ADMIN")

                        // Pedidos en línea: cualquier usuario autenticado puede crear/consultar los suyos;
                        // la gestión completa (listar todos, cambiar estado) es de ADMIN/CAJERO
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAnyAuthority("CLIENTE", "CAJERO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").authenticated()
                        .requestMatchers("/api/pedidos/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Ventas: creación/edición ADMIN y CAJERO; consulta autenticada
                        .requestMatchers(HttpMethod.GET, "/api/ventas/**").authenticated()
                        .requestMatchers("/api/ventas/**").hasAnyAuthority("ADMIN", "CAJERO")
                        .requestMatchers("/api/detalle-ventas/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Carrito: CLIENTE, CAJERO y ADMIN
                        .requestMatchers("/api/carrito/**").hasAnyAuthority("CLIENTE", "CAJERO", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
