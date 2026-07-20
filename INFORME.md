# Informe Técnico — Minimarket Plus (EFT S9)

> **Instrucciones para el equipo:** este documento está organizado 1:1 con los criterios de la pauta de evaluación de la EFT. Cada sección incluye el trabajo ya realizado en el código y **placeholders** `[CAPTURA: ...]` donde deben agregar evidencia visual (capturas de Swagger UI, Postman, consola, reportes JaCoCo/Surefire, etc.) antes de traspasar el contenido a la plantilla oficial del AVA. Reemplacen también los datos de la portada (integrantes, sección, fecha).

## Portada

- **Curso / sección:** [COMPLETAR]
- **Integrantes:** [COMPLETAR — nombre y rol de cada integrante]
- **Jefe de proyecto:** [COMPLETAR]
- **Repositorio:** [COMPLETAR — URL pública del repositorio GitHub]
- **Fecha:** [COMPLETAR]



## Resumen ejecutivo

Minimarket Plus es un backend Spring Boot (monolito modular, un solo servicio desplegable organizado por dominios: seguridad/usuarios, productos/inventario, ventas/pedidos y reportes) que gestiona una cadena de minimarkets con múltiples sucursales. Esta entrega cierra la brecha entre el proyecto base (CRUD con HTTP Basic) y los requerimientos de la EFT S9: autenticación JWT de punta a punta, funcionalidades de negocio del caso (sucursales con stock propio, reposición automática, pedidos en línea con retiro/despacho, promociones, reportes de rotación), calidad transversal (validación, manejo de errores) y una suite de pruebas ampliada.

---



## Criterio 1 — Diseño de microservicios/operaciones de negocio (15 pts)

Se optó por un **monolito modular** (decisión documentada y confirmada con el equipo) en lugar de microservicios desplegables independientes, dado el tamaño del dominio y el tiempo disponible. La modularidad se expresa en la organización interna del código por paquetes de dominio:

- **Productos e inventario:** `Producto`, `Categoria`, `Sucursal`, `StockSucursal`, `Inventario`, `Proveedor`, `OrdenCompra`.
- **Ventas y pedidos:** `Venta`, `DetalleVenta`, `Carrito`, `Pedido`, `DetallePedido`, `Promocion`.
- **Seguridad y usuarios:** `Usuario`, `Rol`, módulo `security` (JWT).
- **Reportes:** agregación sobre `DetalleVenta` para rotación de productos.

Operaciones de negocio implementadas y sus endpoints:

| Funcionalidad         | Endpoint principal                                                                    | Reglas destacadas                                                                                           |
| --------------------- | ------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| Stock por sucursal    | `GET /api/productos/{id}/disponibilidad`, `GET/POST/PUT/DELETE /api/stock-sucursal`   | Cada producto tiene cantidad y stock mínimo independientes por sucursal                                     |
| Reposición automática | `POST /api/inventario` (dispara), `GET/POST/PATCH /api/ordenes-compra`                | Si una salida deja `cantidad <= stockMinimo`, se genera una `OrdenCompra` `PENDIENTE` con `automatica=true` |
| Pedidos en línea      | `POST /api/pedidos`, `GET /api/pedidos/mis-pedidos`, `PATCH /api/pedidos/{id}/estado` | Valida stock, aplica promoción vigente, descuenta/repone stock según el estado                              |
| Promociones           | `GET/POST/PUT/DELETE /api/promociones`                                                | Descuento porcentual vigente entre `fechaInicio` y `fechaFin`, aplicado en pedidos                          |
| Reportes de rotación  | `GET /api/reportes/rotacion-productos`                                                | Productos más y menos vendidos agregando `DetalleVenta`                                                     |


`[CAPTURA: diagrama o listado de módulos/paquetes del proyecto en el IDE]`

`[CAPTURA: ejecución en Postman/Swagger de POST /api/pedidos con promoción aplicada]`

---



## Criterio 2 — Seguridad (JWT + roles) (15 pts)

- **Autenticación stateless con JWT:** `JwtUtil` genera y valida tokens firmados con HMAC-SHA256 (claims: `sub` = username, `roles`, `iat`, `exp`). `JwtAuthenticationFilter` (`OncePerRequestFilter`) intercepta cada petición, extrae el token del header `Authorization: Bearer`, lo valida y puebla el `SecurityContext` con las autoridades reales del usuario (consultadas en la base de datos vía `CustomUserDetailsService`, no confiando ciegamente en el claim del token).
- **Endpoints de autenticación:** `POST /auth/login` (devuelve `AuthResponse` con token, tipo, roles y expiración) y `POST /auth/registro` (autoregistro público de clientes, rol `CLIENTE` fijo).
- `SecurityConfig`**:** sesión `STATELESS`, sin `httpBasic`/`formLogin`, `AuthenticationEntryPoint` que devuelve `401` ante peticiones no autenticadas, y reglas de autorización por método HTTP y rol para cada recurso (ver tabla completa en el [README](README.md#roles-y-control-de-acceso)).
- **Contraseñas:** cifradas con **BCrypt** (`UsuarioServiceImpl#save`), evitando doble cifrado al actualizar un usuario existente.
- **Manejo de errores de seguridad:** `GlobalExceptionHandler` traduce `BadCredentialsException` → 401 y `AccessDeniedException` → 403, con un cuerpo `ErrorResponse` consistente.

Evidencia de pruebas de seguridad (ver Criterio 3): `JwtUtilTest`, `AuthControllerTest`, `JwtAuthenticationTest`, `SecurityAuthorizationTest`.

`[CAPTURA: POST /auth/login exitoso en Swagger/Postman, mostrando el token devuelto]`

`[CAPTURA: acceso a un endpoint protegido con el botón "Authorize" de Swagger UI usando el token]`

`[CAPTURA: intento de acceso sin token o con token de un rol sin permisos → 401/403]`

---



## Criterio 3 — Pruebas unitarias (10 pts)

**Estado:** 132 pruebas, 0 fallos (antes de esta entrega: 42 pruebas). Cobertura de instrucciones JaCoCo ≈ 67% (antes: ≈48%).

Tipos de prueba en la suite:


| Tipo                               | Ejemplos                                                                                                                                                                                          | Qué valida                                                                                                                                                                                                |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Entidades                          | `StockSucursalTest`, `PromocionTest`, `PedidoTest`, `DetallePedidoTest`, `OrdenCompraTest`, `SucursalTest`, `ProveedorTest`                                                                       | Getters/setters y reglas de negocio embebidas (`requiereReposicion()`, `estaVigente()`, `aplicarDescuento()`, `getSubtotal()`)                                                                            |
| Servicios (Mockito)                | `ProductoServiceImplTest`, `SucursalServiceImplTest`, `StockSucursalServiceImplTest`, `OrdenCompraServiceImplTest`, `PromocionServiceImplTest`, `PedidoServiceImplTest`, `ReporteServiceImplTest` | Lógica de negocio con repositorios mockeados: ajuste de stock, disparo de reposición automática, aplicación de descuentos, validación de stock al crear pedidos, ranking de rotación                      |
| Controladores (MockMvc + JWT)      | `AuthControllerTest`, `JwtAuthenticationTest`                                                                                                                                                     | Login/registro, tokens inválidos/expirados/de usuarios inexistentes, autorización por rol end-to-end en los endpoints nuevos (sucursales, reportes, proveedores, órdenes de compra, promociones, pedidos) |
| Integración de flujo de negocio    | `InventarioReposicionAutomaticaTest`                                                                                                                                                              | Flujo completo salida de inventario → actualización de `StockSucursal` → generación automática de `OrdenCompra`, verificando aislamiento entre sucursales                                                 |
| Seguridad / persistencia (previas) | `SecurityAuthorizationTest`, `PersistenciaEntidadesTest`, `JwtUtilTest`                                                                                                                           | Control de acceso por rol, restricciones de persistencia (unicidad, `NOT NULL`), generación/validación de tokens                                                                                          |


`[CAPTURA: resumen de` ./mvnw test `con "Tests run: 132, Failures: 0, Errors: 0"]`

`[CAPTURA: target/site/jacoco/index.html mostrando el % de cobertura total]`

`[CAPTURA: target/reports/surefire.html]`

---



## Criterio 4 — OpenAPI + HATEOAS (10 pts)

- **OpenAPI/Swagger:** `OpenApiConfig` define el `SecurityScheme` `bearerAuth` (HTTP Bearer, formato JWT) y lo aplica globalmente, permitiendo usar el botón **Authorize** de Swagger UI. Todos los controladores (nuevos y existentes) están anotados con `@Tag`, `@Operation` y `@ApiResponses`. El contrato se expone en `/v3/api-docs` y está exportado en `[docs/openapi.json](docs/openapi.json)`.
- **HATEOAS:** todos los controladores devuelven `EntityModel`/`CollectionModel` con enlaces `self` y de navegación relacionados (p. ej. una `Sucursal` enlaza a su `stock`, un `Producto` enlaza a su `disponibilidad`). Se completó HATEOAS en los controladores que antes carecían de él (`CategoriaController`, `VentaController`, `DetalleVentaController`) y se agregó a todos los controladores nuevos (`SucursalController`, `StockSucursalController`, `ProveedorController`, `OrdenCompraController`, `PromocionController`, `PedidoController`, `ReporteController`, `AuthController`).
- Ejemplos de respuestas HATEOAS reales capturados en `[docs/ejemplo-hateoas-login.json](docs/ejemplo-hateoas-login.json)` y `[docs/ejemplo-hateoas-productos.json](docs/ejemplo-hateoas-productos.json)`.

`[CAPTURA: Swagger UI mostrando la lista de tags/controladores documentados]`

`[CAPTURA: respuesta JSON de un endpoint con el bloque` _links`]`

---



## Criterio 5 — Integración de componentes (15 pts)

El flujo de negocio principal integra varias capas y módulos en una sola transacción de dominio:

1. **Venta/pedido → descuento de stock:** al crear un `Pedido` (`PedidoServiceImpl.crearPedido`), se valida el stock disponible en `StockSucursal` para la sucursal indicada, se aplica el descuento de la promoción vigente (`PromocionService.calcularPrecioConDescuento`) al precio unitario, se persiste el pedido con sus `DetallePedido`, y se registra un movimiento `SALIDA` en `Inventario`, el cual a su vez ajusta `StockSucursal` (`StockSucursalService.ajustarStock`).
2. **Descuento de stock → reposición automática:** si ese ajuste deja la cantidad en el mínimo o por debajo, `StockSucursalServiceImpl` invoca a `OrdenCompraService.generarAutomatica`, que crea una `OrdenCompra` `PENDIENTE` asociada a un proveedor (existente o genérico).
3. **Cancelación de pedido → reposición de stock:** al cambiar el estado de un pedido a `CANCELADO` (antes de `ENTREGADO`), se registra un movimiento `ENTRADA` que repone el stock descontado originalmente.

Esta cadena completa (pedido → stock → orden de compra automática) está cubierta por la prueba de integración `InventarioReposicionAutomaticaTest`, que ejercita las capas de repositorio, servicio y JPA reales (sin mocks) contra la base H2.

`[CAPTURA: resultado de InventarioReposicionAutomaticaTest en verde]`

`[CAPTURA: secuencia en Postman: crear pedido → consultar disponibilidad → ver orden de compra generada]`

---



## Criterio 6 — Informe (10 pts)

Este documento. Pendiente para el equipo: completar la portada, agregar las capturas indicadas en cada sección y trasladar el contenido a la plantilla oficial entregada por el AVA.

---



## Criterio 7 — Entrega y checklist final (15 pts)

Checklist a cargo del equipo (fuera del alcance de este agente):

- [ ] Repositorio GitHub **público** con todo el código, README, INFORME.md y `docs/`.
- [ ] Video (Kaltura, 7–10 min) mostrando: arquitectura, login JWT, un flujo de negocio completo (p. ej. pedido → reposición automática) y evidencia de pruebas/cobertura.
- [ ] Video subido al repositorio (o enlazado desde el README).
- [ ] Generación y envío del enlace del repositorio/video al AVA.
- [ ] Jefe de proyecto y cronograma definidos y documentados.

---



## Criterio 8 — Calidad de código (10 pts)

- **DTOs:** `ErrorResponse`, `ProductoRotacionDTO`, `RotacionProductosResponse`, `LoginRequest`, `RegistroRequest`, `AuthResponse` separan la representación de la API de las entidades JPA donde corresponde.
- **Manejo global de excepciones:** `GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza la traducción de `ResourceNotFoundException` (404), `BusinessRuleException` (400), errores de validación (400, con detalle por campo), `BadCredentialsException` (401), `AccessDeniedException` (403) y errores genéricos (500) a un `ErrorResponse` consistente.
- **Eliminación del antipatrón** `findById` **→** `null`**:** todos los servicios (`ProductoService`, `CategoriaService`, `CarritoService`, `InventarioService`, `VentaService`, `DetalleVentaService`, `SucursalService`, `StockSucursalService`, `OrdenCompraService`, `PromocionService`, `PedidoService`) lanzan `ResourceNotFoundException` en lugar de devolver `null`, y los controladores ya no necesitan verificaciones manuales de `null`.
- **Validación de datos:** anotaciones Bean Validation (`@NotBlank`, `@NotNull`, `@Positive`, `@PositiveOrZero`, `@DecimalMin/Max`, `@Size`) en entidades y DTOs, con `@Valid` en los controladores.

`[CAPTURA: ejemplo de respuesta 400 con detalle de errores de validación por campo]`

---



## Anexos

- [README.md](README.md) — instrucciones de ejecución, tabla completa de roles/endpoints, guía de pruebas.
- `[docs/openapi.json](docs/openapi.json)` — contrato OpenAPI exportado.
- `[docs/ejemplo-hateoas-login.json](docs/ejemplo-hateoas-login.json)`, `[docs/ejemplo-hateoas-productos.json](docs/ejemplo-hateoas-productos.json)` — ejemplos de respuestas con hipermedios.

