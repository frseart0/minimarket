package com.minimarket.security.config;

import com.minimarket.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF con la nueva sintaxis
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll() // Acceso público

                        // Documentacion OpenAPI / Swagger UI de acceso público
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // Gestión de usuarios: solo ADMIN
                        .requestMatchers("/api/usuarios/**").hasAuthority("ADMIN")

                        // Edición de productos: escritura solo ADMIN, lectura autenticada
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                        .requestMatchers("/api/productos/**").hasAuthority("ADMIN")

                        // Manejo de inventario: ADMIN y CAJERO
                        .requestMatchers("/api/inventario/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Generación de ventas: crear -> ADMIN y CAJERO; consultar -> autenticado
                        .requestMatchers(HttpMethod.POST, "/api/ventas/**").hasAnyAuthority("ADMIN", "CAJERO")
                        .requestMatchers("/api/ventas/**").authenticated()
                        .requestMatchers("/api/detalle-ventas/**").hasAnyAuthority("ADMIN", "CAJERO")

                        // Carrito: CLIENTE, CAJERO y ADMIN
                        .requestMatchers("/api/carrito/**").hasAnyAuthority("CLIENTE", "CAJERO", "ADMIN")
                        .requestMatchers("/api/categorias/**").authenticated()

                        .anyRequest().authenticated() // Requiere autenticación para el resto
                )
                .httpBasic(basic -> {}) // Autenticación básica para clientes API/pruebas
                .formLogin(form -> form
                        .defaultSuccessUrl("/public/hola", true) // Redirigir después del login
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/public/hola")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Configuración de encriptación de contraseñas
    }
}
