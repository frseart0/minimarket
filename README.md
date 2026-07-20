# Minimarket Plus — Backend

Backend para la gestión integral de una cadena de minimarkets (Minimarket Plus): productos, inventario multi-sucursal, ventas en tienda, pedidos en línea (retiro/despacho), promociones, reposición automática de stock y reportes de rotación de productos. Desarrollado con **Spring Boot 3.4.1** y **Java 17** como un **monolito modular** organizado por dominios de negocio.

Esta entrega (EFT S9) incorpora **autenticación stateless con JWT**, **control de acceso por roles**, **validación de datos**, **manejo global de excepciones**, **documentación OpenAPI/HATEOAS completa** y una **suite de pruebas ampliada** (unitarias, Mockito, MockMvc e integración).

## Tecnologías

- Java 17 · Spring Boot 3.4.1
- Spring Web · Spring Data JPA · Spring Security
- **JWT** (`io.jsonwebtoken` / jjwt 0.12.6) — autenticación sin estado
- **Spring HATEOAS** (enlaces dinámicos) · **springdoc-openapi / Swagger UI** (documentación interactiva)
- **Bean Validation** (`spring-boot-starter-validation`)
- Base de datos en memoria H2
- Maven (con wrapper `mvnw`)
- JUnit 5 · Mockito · Spring Security Test · MockMvc
- JaCoCo (cobertura) + Maven Surefire (reportes)

## Arquitectura

Un único servicio Spring Boot desplegable, organizado internamente en módulos de dominio (no son microservicios independientes, es una decisión consciente de **monolito modular**):

- **Seguridad/Usuarios:** `Usuario`, `Rol`, JWT (`AuthController`, `JwtUtil`, `JwtAuthenticationFilter`).
- **Productos/Inventario:** `Producto`, `Categoria`, `Sucursal`, `StockSucursal` (stock por sucursal), `Inventario` (movimientos), `Proveedor`, `OrdenCompra` (reposición manual/automática).
- **Ventas/Pedidos:** `Venta`, `DetalleVenta` (venta en tienda), `Carrito`, `Pedido`/`DetallePedido` (pedidos en línea con retiro o despacho), `Promocion` (descuentos).
- **Reportes:** rotación de productos (más/menos vendidos) sobre el histórico de ventas.

## Estructura del proyecto

```
src/
├── main/java/com/minimarket/
│   ├── controller/      # Endpoints REST (auth, productos, sucursales, inventario, ordenes-compra,
│   │                    #   ventas, pedidos, promociones, reportes, usuarios, carrito...)
│   ├── entity/          # Entidades JPA + enums (EstadoPedido, EstadoOrdenCompra, TipoEntrega)
│   ├── dto/             # DTOs (ErrorResponse, ProductoRotacionDTO, RotacionProductosResponse)
│   ├── exception/       # ResourceNotFoundException, BusinessRuleException, GlobalExceptionHandler
│   ├── repository/      # Repositorios Spring Data JPA
│   ├── service/         # Interfaces de servicio
│   │   └── impl/        # Implementaciones de servicio (reglas de negocio)
│   ├── security/
│   │   ├── config/      # SecurityConfig (JWT stateless, reglas de autorización por rol)
│   │   ├── filter/       # JwtAuthenticationFilter
│   │   ├── service/     # CustomUserDetailsService
│   │   ├── model/       # LoginRequest, RegistroRequest, AuthResponse
│   │   └── util/        # JwtUtil (generación/validación de tokens)
│   ├── config/          # DataInitializer (datos demo), OpenApiConfig (Swagger + bearer JWT)
│   └── MinimarketApplication.java
├── main/resources/
│   └── application.properties   # Configuración H2, JPA y JWT (secreto/expiración)
└── test/java/com/minimarket/    # Pruebas: entidades, servicios (Mockito), controladores (MockMvc),
                                  # seguridad JWT e integración de flujos de negocio
```

## Autenticación (JWT)

La autenticación es **stateless**: no hay sesiones de servidor, cada petición protegida debe incluir un token JWT en el header `Authorization`.

1. **Login:** `POST /auth/login` con `{ "username": "...", "password": "..." }` → devuelve un `AuthResponse` con el token, tipo (`Bearer`), roles y tiempo de expiración.
2. **Uso del token:** incluir `Authorization: Bearer <token>` en cada petición a un endpoint protegido.
3. **Registro público de clientes:** `POST /auth/registro` con `{ "username": "...", "password": "..." }` crea un usuario con rol `CLIENTE` (autoregistro, sin necesidad de estar autenticado).

El token incluye el `username` y los roles como claims, se firma con HMAC-SHA256 y expira según `jwt.expiration-ms` (por defecto, 1 hora). La clave secreta se configura en `application.properties` (`jwt.secret`), idealmente vía variable de entorno en producción.

Las contraseñas se almacenan cifradas con **BCrypt**.

## Roles y control de acceso

Reglas definidas en `SecurityConfig` (autorización por rol y método HTTP):

| Recurso | CLIENTE | CAJERO | ADMIN |
|---|:---:|:---:|:---:|
| `POST /auth/login`, `POST /auth/registro` | público | público | público |
| `GET /api/productos/**`, `/api/productos/{id}/disponibilidad` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/productos` | ❌ | ❌ | ✅ |
| `GET /api/categorias/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/categorias` | ❌ | ❌ | ✅ |
| `GET /api/sucursales/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/sucursales` | ❌ | ❌ | ✅ |
| `GET /api/stock-sucursal/**` | ❌ | ✅ | ✅ |
| `POST/PUT/DELETE /api/stock-sucursal` | ❌ | ✅ | ✅ |
| `/api/inventario/**` | ❌ | ✅ | ✅ |
| `/api/proveedores/**` | ❌ | ❌ | ✅ |
| `/api/ordenes-compra/**` | ❌ | ✅ | ✅ |
| `/api/reportes/**` | ❌ | ❌ | ✅ |
| `GET /api/promociones/**` | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/promociones` | ❌ | ❌ | ✅ |
| `POST /api/pedidos` | ✅ (propio) | ✅ | ✅ |
| `GET /api/pedidos/mis-pedidos` | ✅ (propio) | ✅ | ✅ |
| `GET /api/pedidos`, `PATCH /api/pedidos/{id}/estado` | ❌ | ✅ | ✅ |
| `GET /api/ventas/**` | ❌ | ✅ | ✅ |
| `POST/PUT/DELETE /api/ventas`, `/api/detalle-ventas/**` | ❌ | ✅ | ✅ |
| `/api/carrito/**` | ✅ | ✅ | ✅ |
| `/api/usuarios/**` | ❌ | ❌ | ✅ |

## Funcionalidades de negocio

- **Sucursales y stock centralizado:** cada producto tiene un stock independiente por sucursal (`StockSucursal`), con `stockMinimo` configurable. `GET /api/productos/{id}/disponibilidad` permite a un cliente ver en qué sucursales hay unidades antes de retirar o pedir despacho.
- **Reposición automática:** al registrar una salida de inventario (`POST /api/inventario`) que deja el stock de una sucursal en su mínimo o por debajo, se genera automáticamente una `OrdenCompra` (`automatica = true`, estado `PENDIENTE`) asociada a un proveedor existente (o uno genérico si no hay ninguno registrado).
- **Órdenes de compra:** además de las automáticas, ADMIN/CAJERO pueden crear órdenes manuales y transicionar su estado (`PENDIENTE → RECIBIDA/CANCELADA`) vía `PATCH /api/ordenes-compra/{id}/estado`.
- **Pedidos en línea:** `POST /api/pedidos` valida stock disponible en la sucursal, aplica automáticamente promociones vigentes al precio y descuenta el stock. Soporta `tipoEntrega`: `RETIRO_TIENDA` o `DESPACHO_DOMICILIO` (con `direccionEntrega`). El flujo de estados es `PENDIENTE → CONFIRMADO → EN_PREPARACION → LISTO → ENTREGADO`, o `CANCELADO` en cualquier punto antes de la entrega (lo que repone el stock descontado).
- **Promociones:** `Promocion` define un descuento porcentual vigente entre dos fechas para un producto; se aplica automáticamente al calcular el precio unitario de un pedido.
- **Reportes de rotación:** `GET /api/reportes/rotacion-productos` devuelve los productos más y menos vendidos (agregando `DetalleVenta`), útil para decisiones de reposición y descuentos.

## Cómo ejecutar

Requisitos: JDK 17+ instalado (se validó con JDK 20).

```bash
# Levantar la aplicación (http://localhost:8080)
./mvnw spring-boot:run
```

Al iniciar se cargan datos demo: usuarios/roles, categorías y productos, dos sucursales con su stock inicial (una cercana al mínimo, para poder probar la reposición automática), un proveedor y una promoción vigente.

**Credenciales demo:**

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `cajero` | `cajero123` | CAJERO |
| `cliente` | `cliente123` | CLIENTE |

Verificación rápida del flujo JWT (con la app corriendo):

```bash
# 1) Endpoint público, sin autenticación
curl -i http://localhost:8080/public/hola

# 2) Sin token: 401
curl -i http://localhost:8080/api/productos

# 3) Login: obtener el token
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"cliente","password":"cliente123"}'

# 4) Usar el token recibido para acceder a un recurso protegido
curl -i http://localhost:8080/api/productos \
  -H "Authorization: Bearer <token-obtenido-en-el-paso-3>"
```

## Documentación de la API (OpenAPI + HATEOAS)

Con la aplicación corriendo:

- **Swagger UI:** http://localhost:8080/swagger-ui.html — usar el botón **Authorize** con `Bearer <token>` (obtenido en `POST /auth/login`) para probar los endpoints protegidos directamente desde la interfaz.
- **Contrato OpenAPI (JSON):** http://localhost:8080/v3/api-docs — también exportado en [`docs/openapi.json`](docs/openapi.json) (importable en Postman).

Las respuestas incluyen enlaces hipermedia (`_links`) generados con Spring HATEOAS. Ver ejemplos en [`docs/ejemplo-hateoas-productos.json`](docs/ejemplo-hateoas-productos.json) y [`docs/ejemplo-hateoas-login.json`](docs/ejemplo-hateoas-login.json).

## Pruebas y reportes

```bash
# Ejecutar todas las pruebas + generar cobertura JaCoCo
./mvnw clean test

# Generar el reporte de cobertura (target/site/jacoco) e informe de pruebas
./mvnw clean verify

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

**Estado actual:** 132 pruebas, 0 fallos. Cobertura de instrucciones ≈ 67% (antes de esta entrega: 42 pruebas, ≈48%). La suite incluye:

- Pruebas de entidades (getters/setters, reglas de negocio como `requiereReposicion()`, `estaVigente()`, `aplicarDescuento()`, `getSubtotal()`).
- Pruebas de servicios con **Mockito** (repositorios mockeados): `ProductoService`, `SucursalService`, `StockSucursalService` (incluye la lógica de reposición automática), `OrdenCompraService`, `PromocionService`, `PedidoService`, `ReporteService`.
- Pruebas de controladores con **MockMvc** y JWT real (`AuthControllerTest`, `JwtAuthenticationTest`), validando login, registro, tokens inválidos/expirados/de usuarios inexistentes y autorización por rol en los endpoints nuevos.
- Prueba de integración end-to-end (`InventarioReposicionAutomaticaTest`) del flujo completo: salida de inventario → actualización de `StockSucursal` → generación automática de `OrdenCompra` cuando corresponde, verificando además que no se afecta el stock de otras sucursales.

## Documentación

El análisis técnico completo (configuración de seguridad, evidencia de pruebas, mejoras aplicadas y documentación OAS/HATEOAS) mapeado a la pauta de evaluación está en [`INFORME.md`](INFORME.md).
