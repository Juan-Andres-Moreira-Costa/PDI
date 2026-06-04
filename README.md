# SIENEP — API RESTful
## Sistema de Seguimiento Integral de Estudiantes con Necesidades Educativas Personalizadas
**Universidad Tecnológica (UTEC) — Proyecto de Desarrollo e Infraestructura 2026**
**Equipo: Boys del Tre — Juan Andrés Moreira Costa**

---

## Descripción

API RESTful desarrollada con Java 17 y Spring Boot 3.2.5 como backend del sistema SIENEP. Gestiona estudiantes, instancias, recordatorios, incidencias, roles y reportes institucionales con seguridad JWT, auditoría completa y control de acceso basado en roles.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Security + JWT (JJWT) | 0.12.5 |
| Spring Data JPA / Hibernate | 6.x |
| PostgreSQL | 15 |
| Springdoc OpenAPI (Swagger UI) | 2.5.0 |
| JUnit 5 + Mockito | Spring Boot Test |
| Maven | 3.9.x |
| Docker / Docker Compose | — |

---

## Arquitectura del Proyecto

```
src/main/java/com/utec/sienep/
├── config/          SecurityConfig, SwaggerConfig, DataInitializer
├── controller/      10 controladores REST
├── dto/
│   ├── request/     8 DTOs de entrada con validaciones Jakarta
│   └── response/    7 DTOs de salida (RNF01: @JsonInclude para campos sensibles)
├── entity/          10 entidades JPA
├── exception/       Excepciones personalizadas + GlobalExceptionHandler
├── repository/      10 repositorios Spring Data JPA
├── security/        JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
├── service/         9 servicios con lógica de negocio
└── util/            ValidacionUtil (cédula uruguaya + edad mínima)
```

---

## Nomenclatura de Base de Datos

Convenciones aplicadas según especificación del proyecto:

| Tipo | Convención | Ejemplo |
|---|---|---|
| Tabla (1 palabra) | Plural | `USUARIOS`, `ROLES` |
| Tabla (2 palabras) | 4 chars primera + _ + segunda | `CATE_INSTANCIAS`, `INFO_MEDICOS` |
| Columna (1 palabra) | Singular | `NOMBRE`, `ESTADO` |
| Columna (2 palabras) | 3 chars primera + _ + segunda | `FEC_ALTA`, `MOT_BAJA` |
| Clave primaria | `PK_[entidad_singular]` | `PK_USUARIO`, `PK_INSTANCIA` |
| Clave foránea | `FK_[abrev_tabla]_[abrev_columna]` | `FK_INST_ESTUDIANTE` |
| Clave única | `UK_[abrev_tabla]_[columna]` | `UK_ESTU_CEDULA` |

---

## Requerimientos implementados

### Módulo 1 — Autenticación y Acceso
RF01 RF02 RF03 RF04 — Login JWT, logout, gestión de credenciales, BCrypt

### Módulo 2 — Gestión de Estudiantes
RF05 RF06 RF07 RF08 RF09 — CRUD completo, baja lógica, informes médicos

### Módulo 3 — Gestión de Instancias
RF10 al RF18 — Registro, categorización, identificador automático, clonación, Google Calendar (preparado)

### Módulo 4 — Gestión de Recordatorios
RF19 al RF27 — Creación, recurrencia, notificaciones, ID automático, instancia desde recordatorio

### Módulo 5 — Incidencias
RF28 RF29 — Registro con severidad, historial completo por estudiante

### Módulo 7 — Administración
RF32 RF33 RF34 RF35 RF36 RF37 RF38 RF39 RF40 — Roles, categorías de instancias y recordatorios

### Requerimientos No Funcionales
- **RNF01/RD01** — Campos sensibles (`inf_salud`, `obs_confidencial`) controlados por rol en servicio y DTO
- **RNF02** — Contraseñas con BCryptPasswordEncoder
- **RNF03** — Control de acceso con `@PreAuthorize` y roles
- **RNF04** — AuditoriaService registra todas las operaciones con usuario, fecha y acción
- **RNF05** — Eliminación lógica en todas las entidades (campo `activo`)
- **RNF10** — Docker Compose para ejecución multiplataforma
- **RNF12** — NotificacionService con JavaMailSender (simulado en desarrollo)

---

## Instalación y ejecución

### Con Docker (recomendado)

```bash
docker-compose up --build
```

### Local (sin Docker)

1. Crear la base de datos:
```bash
psql -U proyecto -d sienep -f src/main/resources/db/init.sql
```

2. Ejecutar:
```bash
mvn clean spring-boot:run
```

---

## Acceso

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

---

## Autenticación

Al iniciar la aplicación se crea automáticamente el usuario administrador:

| Campo | Valor |
|---|---|
| Username | `admin` |
| Contraseña | `Admin1234!` |

**Cambiar la contraseña en el primer login** usando `PUT /api/v1/auth/cambiar-password`.

### Flujo en Swagger UI
1. `POST /api/v1/auth/login` con las credenciales
2. Copiar el token de la respuesta
3. Clic en **Authorize** (🔒) → ingresar `<token>`
4. Todos los endpoints protegidos quedan habilitados

---

## Endpoints principales

| Módulo | Base URL |
|---|---|
| Autenticación | `/api/v1/auth` |
| Estudiantes | `/api/v1/estudiantes` |
| Instancias | `/api/v1/instancias` |
| Recordatorios | `/api/v1/recordatorios` |
| Incidencias | `/api/v1/incidencias` |
| Informes Médicos | `/api/v1/estudiantes/{id}/informes-medicos` |
| Categorías Instancias | `/api/v1/categorias-instancias` |
| Categorías Recordatorios | `/api/v1/categorias-recordatorios` |
| Administración | `/api/v1/admin` |
| Auditoría | `/api/v1/auditoria` |

---

## Tests

```bash
mvn test
```

| Clase | Tests | Cubre |
|---|---|---|
| `ValidacionUtilTest` | 11 | Cédula uruguaya + edad mínima |
| `EstudianteServiceTest` | 9 | CRUD de estudiantes + auditoría |
| `AuthServiceTest` | 5 | Login, logout, cambio de contraseña |
| `InstanciaServiceTest` | 6 | Instancias + clonación |
| `RecordatorioServiceTest` | 8 | Recordatorios + recurrencia + RF27 |
| `IncidenciaServiceTest` | 6 | Registro + historial + cambio de estado |
| `JwtUtilTest` | 5 | Generación, validación y expiración de JWT |
| **Total** | **50** | |

---

## Estrategia Git (Gitflow)

```
main          ← versiones estables de cada entrega
└── develop   ← integración continua
    ├── feature/setup-proyecto
    ├── feature/crud-estudiantes
    ├── feature/validaciones
    ├── feature/swagger-tests
    ├── feature/seguridad-jwt
    ├── feature/auth-controller
    ├── feature/modulo-instancias
    ├── feature/informes-medicos
    ├── feature/auditoria
    ├── feature/modulo-recordatorios
    ├── feature/modulo-incidencias
    └── feature/administracion-roles
```
