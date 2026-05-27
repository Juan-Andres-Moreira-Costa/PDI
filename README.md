# SIENEP — API RESTful
### Sistema de Seguimiento Integral de Estudiantes con Necesidades Educativas Personalizadas
**Universidad Tecnológica (UTEC) — Proyecto de Desarrollo e Infraestructura 2026**

---

## Descripción

API RESTful desarrollada con Java 17 y Spring Boot 3 como backend del sistema SIENEP, que gestiona estudiantes, instancias, recordatorios, incidencias, roles y reportes institucionales.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Data JPA / Hibernate | 6.x |
| PostgreSQL | 15 |
| Springdoc OpenAPI (Swagger) | 2.5.0 |
| JUnit 5 + Mockito | (incluido en Spring Boot Test) |
| Maven | 3.9.x |
| Docker / Docker Compose | - |

---

## Arquitectura del Proyecto

```
src/
└── main/
    ├── java/com/utec/sienep/
    │   ├── config/          # Configuración (Swagger, etc.)
    │   ├── controller/      # Controladores REST
    │   ├── dto/
    │   │   ├── request/     # DTOs de entrada
    │   │   └── response/    # DTOs de salida
    │   ├── entity/          # Entidades JPA
    │   ├── exception/       # Excepciones personalizadas y handler global
    │   ├── repository/      # Repositorios Spring Data JPA
    │   ├── service/         # Lógica de negocio
    │   └── util/            # Utilidades (validaciones)
    └── resources/
        ├── application.properties
        └── db/
            └── init.sql     # Script DDL de inicialización
```

---

## Requisitos previos

- JDK 17
- Maven 3.9+
- PostgreSQL 15 (o Docker)

---

## Configuración local (sin Docker)

1. Crear la base de datos:
```sql
CREATE DATABASE sienep;
```

2. Ejecutar el script de inicialización:
```bash
psql -U proyecto -d sienep -f src/main/resources/db/init.sql
```

3. Configurar credenciales en `application.properties` o con variables de entorno:
```
DB_URL=jdbc:postgresql://localhost:5432/sienep
DB_USER=proyecto
DB_PASSWORD=proyecto2025
```

4. Compilar y ejecutar:
```bash
mvn clean spring-boot:run
```

---

## Ejecución con Docker

```bash
docker-compose up --build
```

La API queda disponible en `http://localhost:8080`

---

## Documentación interactiva (Swagger UI)

Una vez levantada la aplicación:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

---

## Endpoints disponibles — Entrega 1

### Gestión de Estudiantes (`/api/v1/estudiantes`)

| Método | Endpoint | Descripción | RF |
|---|---|---|---|
| POST | `/api/v1/estudiantes` | Alta de estudiante | RF05 |
| GET | `/api/v1/estudiantes` | Listar / buscar activos | RF08 |
| GET | `/api/v1/estudiantes/{id}` | Buscar por ID | RF08 |
| PUT | `/api/v1/estudiantes/{id}` | Modificar estudiante | RF07 |
| DELETE | `/api/v1/estudiantes/{id}` | Baja lógica | RF06 |

### Parámetros de búsqueda (GET `/api/v1/estudiantes`)
- `?nombre=Juan` — Búsqueda parcial por nombre o apellido
- `?cedula=12345670` — Búsqueda exacta por cédula

---

## Validaciones implementadas

- **Cédula uruguaya:** algoritmo oficial del dígito verificador (acepta 7 u 8 dígitos)
- **Edad mínima:** el estudiante debe tener al menos 18 años
- **Email único:** no se permiten duplicados
- **Cédula única:** no se permiten duplicados
- **Baja lógica:** los registros no se eliminan físicamente; se marcan con `activo = false`

---

## Patrones de diseño aplicados

- **DTO (Data Transfer Object):** obligatorio según consigna; desacopla el modelo interno de la API pública
- **Repository Pattern:** via Spring Data JPA (`EstudianteRepository`)
- **Service Layer:** lógica de negocio centralizada en `EstudianteService`

---

## Pruebas

```bash
mvn test
```

Incluye tests unitarios con JUnit 5 y Mockito para:
- `ValidacionUtilTest` — validación de cédula uruguaya y edad mínima
- `EstudianteServiceTest` — lógica de negocio del servicio de estudiantes

---

## Autores

- **[Nombre Integrante 1]**
- **[Nombre Integrante 2]**
