-- ============================================================
-- SIENEP — Script de inicialización de base de datos
-- Universidad Tecnológica (UTEC) — 2026
-- ============================================================

-- Crear el schema si no existe
CREATE SCHEMA IF NOT EXISTS proyecto;

-- ============================================================
-- Tabla: estudiantes
-- Entidad principal del módulo de Gestión de Estudiantes (RF05-RF09)
-- ============================================================
CREATE TABLE IF NOT EXISTS proyecto.estudiantes (
    id                  BIGSERIAL PRIMARY KEY,
    cedula              VARCHAR(8)   NOT NULL UNIQUE,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    fecha_nacimiento    DATE         NOT NULL,
    telefono            VARCHAR(20),
    direccion           VARCHAR(250),
    itr                 VARCHAR(100),
    carrera             VARCHAR(150),
    grupo               VARCHAR(50),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_baja          TIMESTAMP,
    fecha_modificacion  TIMESTAMP,
    motivo_baja         VARCHAR(500)
);

-- Índices útiles para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_estudiantes_cedula  ON proyecto.estudiantes (cedula);
CREATE INDEX IF NOT EXISTS idx_estudiantes_email   ON proyecto.estudiantes (email);
CREATE INDEX IF NOT EXISTS idx_estudiantes_activo  ON proyecto.estudiantes (activo);
CREATE INDEX IF NOT EXISTS idx_estudiantes_nombre  ON proyecto.estudiantes (nombre, apellido);

-- ============================================================
-- Comentarios de columnas para documentación interna
-- ============================================================
COMMENT ON TABLE  proyecto.estudiantes                    IS 'Registro de estudiantes del sistema SIENEP';
COMMENT ON COLUMN proyecto.estudiantes.cedula             IS 'Cédula de identidad uruguaya (8 dígitos, con cero adelante si es necesario)';
COMMENT ON COLUMN proyecto.estudiantes.activo             IS 'TRUE = activo, FALSE = dado de baja lógicamente';
COMMENT ON COLUMN proyecto.estudiantes.motivo_baja        IS 'Motivo obligatorio al dar de baja lógica (RF06)';
COMMENT ON COLUMN proyecto.estudiantes.fecha_modificacion IS 'Fecha de la última modificación de datos (RF07)';
