# OrderFlow API

[![CI Pipeline](https://github.com/TU_USUARIO/orderflow/actions/workflows/ci.yml/badge.svg)](https://github.com/TU_USUARIO/orderflow/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x%2F4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)

Backend transaccional de alta concurrencia diseñado bajo principios de **Domain-Driven Design (DDD)** táctico en un monolito modular. Resuelve desafíos críticos de consistencia e integridad de datos en e-commerce: prevención estricta de sobreventa (*overbooking*), eliminación de condiciones de carrera (*race conditions*), cobros duplicados mediante idempotencia persistida y optimización de consultas SQL.

