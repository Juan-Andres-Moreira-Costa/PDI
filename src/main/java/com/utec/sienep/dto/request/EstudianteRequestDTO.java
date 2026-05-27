package com.utec.sienep.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class EstudianteRequestDTO {

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 7, max = 8, message = "La cédula debe tener entre 7 y 8 dígitos")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "La cédula debe contener solo números")
    private String cedula;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s'\\-]+$", message = "El nombre solo puede contener letras")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s'\\-]+$", message = "El apellido solo puede contener letras")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 150, message = "El email no puede superar los 150 caracteres")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    @Pattern(regexp = "^[0-9\\+\\-\\s]{7,20}$", message = "El teléfono tiene un formato inválido")
    private String telefono;

    @Size(max = 250, message = "La dirección no puede superar los 250 caracteres")
    private String direccion;

    @Size(max = 100, message = "El ITR no puede superar los 100 caracteres")
    private String itr;

    @Size(max = 150, message = "La carrera no puede superar los 150 caracteres")
    private String carrera;

    @Size(max = 50, message = "El grupo no puede superar los 50 caracteres")
    private String grupo;

    // ===================== Getters y Setters =====================

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getItr() { return itr; }
    public void setItr(String itr) { this.itr = itr; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
}
