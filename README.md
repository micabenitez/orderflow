# OrderFlow API

[![CI Pipeline](https://github.com/micabenitez/orderflow/actions/workflows/ci.yml/badge.svg)](https://github.com/TU_USUARIO/orderflow/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?logo=swagger&logoColor=black)]()

Backend transaccional de alta concurrencia diseñado bajo principios de **Domain-Driven Design (DDD)** táctico en un monolito modular. Resuelve desafíos críticos de consistencia e integridad de datos en e-commerce: prevención estricta de sobreventa (*overbooking*), eliminación de condiciones de carrera (*race conditions*), cobros duplicados mediante idempotencia persistida y optimización de consultas SQL.

# Decisiones de Arquitectura e Ingeniería

## 1. Control de Concurrencia e Inventario: Bloqueo Pesimista Determinístico

**Desafío:** En escenarios de alta contención (liquidaciones o lanzamientos), múltiples peticiones concurrentes compiten por el stock del mismo producto, generando condiciones de carrera (*race conditions*) y sobreventa.

**Decisión:** Se implementó `PESSIMISTIC_WRITE` (`SELECT FOR UPDATE`) a nivel de base de datos en `ProductRepository`. A diferencia del bloqueo optimista, el pesimista evita el overhead de reintentos continuos y saturación de CPU bajo ráfagas intensas de tráfico.

**Prevención de Deadlocks:** Para evitar abrazos mortales cuando un pedido contiene múltiples ítems, los identificadores de productos se ordenan canónicamente por UUID antes de adquirir los bloqueos transaccionales, garantizando una secuencia estricta tanto en la compra como en la cancelación.

---

## 2. Idempotencia en Pasarela de Pagos

**Desafío:** Caídas de conexión, timeouts o reintentos del cliente pueden disparar cobros duplicados para una misma orden.

**Decisión:** Se diseñó una tabla de deduplicación (`payment_idempotency_records`) con un índice único en base de datos sobre la cabecera `Idempotency-Key`. Ante un reintento, el sistema detecta la clave existente y retorna la respuesta persistida original sin alterar la máquina de estados ni procesar un nuevo cobro.

---

## 3. Mitigación del Problema de Consultas N+1

**Desafío:** Cargar agregados complejos con colecciones perezosas (`FetchType.LAZY`) suele ejecutar 1 consulta inicial más N consultas secundarias al acceder a los ítems.

**Decisión:** Se implementó una consulta dirigida con `LEFT JOIN FETCH` (`findByIdWithItems`) en `OrderRepository`, resolviendo la orden completa y sus líneas de detalle en un único viaje de red (*round-trip*) a la base de datos.

---

## 4. Consistencia y Desacoplamiento de Eventos

**Desafío:** Disparar notificaciones o interactuar con servicios externos dentro de la transacción activa puede causar bloqueos prolongados o disparar alertas falsas si la base de datos ejecuta un `ROLLBACK`.

**Decisión:** Se desacopló la emisión mediante eventos de Spring utilizando `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`, asegurando que las notificaciones solo se procesen cuando la transacción fue confirmada exitosamente.

---

## 5. Respuestas de Error Estandarizadas (RFC 7807)

La API no expone trazas internas (*stack traces*). Todas las excepciones y violaciones de estado se devuelven bajo el estándar **RFC 7807** (`ProblemDetail`), clasificando semánticamente:

- **400 Bad Request:** Errores de validación de campos y argumentos ilegales.
- **404 Not Found:** Entidades inexistentes en base de datos (`ResourceNotFoundException`).
- **409 Conflict:** Violaciones de la máquina de estados (`IllegalStateException`), como intentar cancelar un pedido ya pagado o cancelado.

---

## 📐 Estructura del Monolito Modular

El proyecto organiza el código separando las responsabilidades de dominio, aplicación e infraestructura por contexto delimitado:

```plaintext
src/main/java/com/mb/orderflow/
├── product/               # Contexto: Catálogo e Inventario
│   ├── domain/            # Entidades de dominio ricas (Product) y Repository con Locks
│   ├── service/           # Casos de uso de catálogo
│   └── api/               # ProductController y DTOs inmutables (Java Records)
├── order/                 # Contexto: Órdenes de Compra
│   ├── domain/            # Aggregate Root (Order, OrderItem, OrderStatus)
│   ├── service/           # Orquestación transaccional, stock y cancelación
│   └── api/               # OrderController y DTOs
├── payment/               # Contexto: Procesamiento de Pagos
│   ├── domain/            # Entidades, IdempotencyRecord y Eventos (PaymentApprovedEvent)
│   ├── service/           # Verificación de idempotencia y cobro
│   └── api/               # PaymentController
└── infrastructure/        # Componentes transversales
    ├── config/            # DataSeeder, OpenApiConfig
    ├── exception/         # GlobalExceptionHandler (RFC 7807)
    └── notification/      # Listeners desacoplados (@TransactionalEventListener)
```

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 (LTS)
- **Framework:** Spring Boot (Spring Data JPA, Spring Web, Validation)
- **Persistencia:** PostgreSQL 16 (Producción / Local), H2 Database (Suite de Tests)
- **Documentación:** Springdoc OpenAPI / Swagger UI
- **Contenedores:** Docker (Multi-stage build) y Docker Compose
- **Testing:** JUnit 5, AssertJ, Spring Boot Test (CountDownLatch + ExecutorService)
- **CI/CD:** GitHub Actions

---

## 🚦 Endpoints de la API

| Verbo | Endpoint | Descripción | Estados HTTP |
|-------|----------|-------------|--------------|
| GET | `/api/v1/products` | Lista los productos y stock del catálogo | 200 |
| POST | `/api/v1/orders` | Crea una orden y reserva stock con bloqueo pesimista | 201, 400 |
| GET | `/api/v1/orders/{orderId}` | Obtiene el detalle completo de la orden (JOIN FETCH) | 200, 404 |
| PATCH | `/api/v1/orders/{orderId}/cancel` | Cancela pedido y restituye el stock reservado | 200, 404, 409 |
| POST | `/api/v1/orders/{orderId}/payments` | Procesa el pago con cabecera requerida `Idempotency-Key` | 200, 400, 409 |

**Documentación interactiva:** Al iniciar la aplicación, la consola de Swagger UI queda disponible en:

```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Validación de Concurrencia (Stress Testing)

Para garantizar la integridad transaccional frente a condiciones de carrera reales, el proyecto incluye `OrderConcurrencyIntegrationTest`.

La prueba simula **10 hilos concurrentes** compitiendo en el mismo milisegundo por **1 única unidad disponible** de un producto, utilizando `CountDownLatch` para sincronizar el disparo:

```bash
mvn test -Dtest=OrderConcurrencyIntegrationTest
```

**Resultados verificados:**

1. **1 orden exitosa** (201 Created).
2. **9 órdenes rechazadas** (400 Bad Request por stock insuficiente).
3. **Stock final en base de datos = 0** (garantía matemática contra overbooking).
