package com.minimarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI expuesta en Swagger UI y /v3/api-docs,
 * incluyendo el esquema de seguridad JWT (bearer) para poder autorizar las
 * pruebas directamente desde Swagger UI ("Authorize" con el token de /auth/login).
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minimarket Plus API")
                        .description("API REST del backend de Minimarket Plus: autenticación JWT, productos, "
                                + "categorías, sucursales y stock centralizado, inventario, órdenes de compra "
                                + "(manuales y automáticas por stock mínimo), ventas, carrito, pedidos en línea "
                                + "(retiro en tienda / despacho a domicilio), promociones, reportes de rotación "
                                + "de productos y usuarios. Documentada con OpenAPI e hipermedios HATEOAS. "
                                + "Los recursos están protegidos por roles (CLIENTE, CAJERO, ADMIN). "
                                + "Para probar los endpoints protegidos: 1) POST /auth/login con las credenciales "
                                + "demo, 2) copiar el token recibido y usar el botón 'Authorize' con 'Bearer <token>'.")
                        .version("2.0.0")
                        .contact(new Contact().name("Equipo Minimarket Plus"))
                        .license(new License().name("Uso académico - Duoc UC")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en POST /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
