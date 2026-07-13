# Minimarket Plus — Backend

Sistema backend para la gestión de un minimarket (productos, inventario, ventas, carritos y usuarios), desarrollado con **Spring Boot 3.4.1** y **Java 17**. Esta entrega incorpora **autenticación, control de acceso por roles** y **pruebas unitarias** que validan la seguridad.

## Tecnologías

- Java 17 · Spring Boot 3.4.1
- Spring Web · Spring Data JPA · Spring Security
- **Spring HATEOAS** (enlaces dinámicos) · **springdoc-openapi / Swagger UI** (documentación)
- Base de datos en memoria H2
- Lombok
- Maven (con wrapper `mvnw`)
- JUnit 5 + Spring Security Test
- JaCoCo (cobertura) + Maven Surefire (reportes)

## Estructura del proyecto

```
src/
├── main/java/com/minimarket/
│   ├── controller/      # Endpoints REST (productos, inventario, ventas, usuarios, carrito...)
│   ├── entity/          # Entidades JPA (Producto, Categoria, Inventario, Carrito, Venta, DetalleVenta, Usuario, Rol)
│   ├── repository/      # Repositorios Spring Data JPA
│   ├── service/         # Interfaces de servicio
│   │   └── impl/        # Implementaciones de servicio
│   ├── security/
│   │   ├── config/      # SecurityConfig (reglas de autorización por rol)
│   │   ├── service/     # CustomUserDetailsService
│   │   ├── model/       # CustomUserDetails, LoginRequest
│   │   └── util/        # JwtUtil (base para JWT)
│   └── MinimarketApplication.java
├── main/resources/
│   └── application.properties   # Configuración H2 y JPA
└── test/java/com/minimarket/    # Pruebas unitarias (entidades, persistencia y seguridad)
```

## Roles y control de acceso

La autenticación se basa en la relación `Usuario` ↔ `Rol` (`@ManyToMany`). Reglas definidas en `SecurityConfig`:

| Recurso / Operación | CLIENTE | CAJERO | ADMIN |
|---|:---:|:---:|:---:|
| `GET /api/productos` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/productos` | ❌ | ❌ | ✅ |
| `/api/inventario` | ❌ | ✅ | ✅ |
| `POST /api/ventas` | ❌ | ✅ | ✅ |
| `/api/carrito` | ✅ | ✅ | ✅ |
| `/api/usuarios` | ❌ | ❌ | ✅ |
| `/public/**` | acceso público | | |

Las contraseñas se almacenan cifradas con **BCrypt**.

## Cómo ejecutar

Requisitos: JDK 17 instalado.

```bash
# Levantar la aplicación (http://localhost:8080)
./mvnw spring-boot:run
```

Al iniciar se cargan datos demo (usuarios, roles y productos).

**Credenciales demo:**

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `cajero` | `cajero123` | CAJERO |
| `cliente` | `cliente123` | CLIENTE |

Verificación rápida de la seguridad (con la app corriendo):

```bash
curl -i http://localhost:8080/public/hola                      # 200 OK (público)
curl -i http://localhost:8080/api/productos                     # 401 (protegido)
curl -i -u cliente:cliente123 http://localhost:8080/api/productos  # 200 + enlaces HATEOAS
```

## Documentación de la API (OpenAPI + HATEOAS)

Con la aplicación corriendo:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Contrato OpenAPI (JSON):** http://localhost:8080/v3/api-docs — también exportado en [`docs/openapi.json`](docs/openapi.json) (importable en Postman).

Las respuestas incluyen enlaces hipermedia (`_links`) generados con Spring HATEOAS. Ver ejemplo en [`docs/ejemplo-hateoas-productos.json`](docs/ejemplo-hateoas-productos.json).

## Pruebas y reportes

```bash
# Ejecutar todas las pruebas + generar cobertura JaCoCo
./mvnw clean test

# Generar reporte HTML de pruebas (Surefire)
./mvnw surefire-report:report -DshowSuccess=true
```

Reportes generados:

| Reporte | Ruta |
|---|---|
| Pruebas (XML) | `target/surefire-reports/TEST-*.xml` |
| Pruebas (HTML) | `target/reports/surefire.html` |
| Cobertura (HTML) | `target/site/jacoco/index.html` |
| Cobertura (XML) | `target/site/jacoco/jacoco.xml` |

**Estado actual:** 42 pruebas, 0 fallos. Cobertura de instrucciones ≈ 48% (entidades 97%, `security.config` 100%).

## Documentación

El análisis técnico completo de las pruebas y de seguridad está en [`INFORME.md`](INFORME.md).
# minimarket
