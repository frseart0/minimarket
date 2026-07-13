package com.minimarket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI expuesta en Swagger UI y /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minimarket Plus API")
                        .description("API REST del backend de Minimarket Plus: productos, carrito, "
                                + "inventario, ventas y usuarios. Documentada con OpenAPI e "
                                + "hipermedios HATEOAS. Los recursos están protegidos por roles "
                                + "(CLIENTE, CAJERO, ADMIN).")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipo Minimarket Plus"))
                        .license(new License().name("Uso académico - Duoc UC")));
    }
}
