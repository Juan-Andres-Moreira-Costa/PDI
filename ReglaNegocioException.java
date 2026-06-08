package com.utec.sienep.dto.response;

import java.util.List;

public class LoginResponseDTO {

    private String token;
    private String tipo = "Bearer";
    private String username;
    private String nombre;
    private String apellido;
    private List<String> roles;

    public LoginResponseDTO(String token, String username,
                            String nombre, String apellido,
                            List<String> roles) {
        this.token = token;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.roles = roles;
    }

    // Nunca incluir password ni passwordHash en este DTO
    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public List<String> getRoles() { return roles; }
}
