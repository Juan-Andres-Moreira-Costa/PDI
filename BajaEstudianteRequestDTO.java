package com.utec.sienep.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de ValidacionUtil — cédula uruguaya y edad mínima")
class ValidacionUtilTest {

    // ===================== Cédula Uruguaya =====================

    @Test
    @DisplayName("Cédula válida de 8 dígitos debe pasar la validación")
    void cedula_valida_8_digitos() {
        // Cédula de ejemplo con dígito verificador correcto
        assertTrue(ValidacionUtil.validarCedulaUruguaya("12345670"));
    }

    @Test
    @DisplayName("Cédula válida de 7 dígitos debe rellenarse con cero y pasar")
    void cedula_valida_7_digitos_se_rellena() {
        // "1234567" -> "01234567" -> debe validar si el dígito verificador es correcto
        // Probamos con una cédula real de 7 dígitos conocida como válida
        assertTrue(ValidacionUtil.validarCedulaUruguaya("2345678"));
    }

    @Test
    @DisplayName("Cédula con dígito verificador incorrecto debe fallar")
    void cedula_digito_verificador_incorrecto() {
        assertFalse(ValidacionUtil.validarCedulaUruguaya("12345671"));
    }

    @Test
    @DisplayName("Cédula nula debe fallar")
    void cedula_nula() {
        assertFalse(ValidacionUtil.validarCedulaUruguaya(null));
    }

    @Test
    @DisplayName("Cédula vacía debe fallar")
    void cedula_vacia() {
        assertFalse(ValidacionUtil.validarCedulaUruguaya(""));
    }

    @Test
    @DisplayName("Cédula con letras debe fallar")
    void cedula_con_letras() {
        assertFalse(ValidacionUtil.validarCedulaUruguaya("1234AB67"));
    }

    @Test
    @DisplayName("Cédula con menos de 7 dígitos debe fallar")
    void cedula_muy_corta() {
        assertFalse(ValidacionUtil.validarCedulaUruguaya("12345"));
    }

    @Test
    @DisplayName("Cédula con más de 8 dígitos debe fallar")
    void cedula_muy_larga() {
        assertFalse(ValidacionUtil.validarCedulaUruguaya("123456789"));
    }

    // ===================== Edad Mínima =====================

    @Test
    @DisplayName("Persona con exactamente 18 años debe ser válida")
    void edad_exactamente_18_anios() {
        LocalDate hace18 = LocalDate.now().minusYears(18);
        assertTrue(ValidacionUtil.esMayorDeEdad(hace18));
    }

    @Test
    @DisplayName("Persona con más de 18 años debe ser válida")
    void edad_mayor_de_18_anios() {
        LocalDate hace25 = LocalDate.now().minusYears(25);
        assertTrue(ValidacionUtil.esMayorDeEdad(hace25));
    }

    @Test
    @DisplayName("Persona con 17 años debe fallar")
    void edad_menor_de_18_anios() {
        LocalDate hace17 = LocalDate.now().minusYears(17);
        assertFalse(ValidacionUtil.esMayorDeEdad(hace17));
    }

    @Test
    @DisplayName("Persona con 17 años y 364 días debe fallar")
    void edad_casi_18_anios() {
        LocalDate casiMayor = LocalDate.now().minusYears(18).plusDays(1);
        assertFalse(ValidacionUtil.esMayorDeEdad(casiMayor));
    }

    @Test
    @DisplayName("Fecha de nacimiento nula debe fallar")
    void edad_fecha_nula() {
        assertFalse(ValidacionUtil.esMayorDeEdad(null));
    }

    // ===================== Email =====================

    @Test
    @DisplayName("Email válido debe pasar")
    void email_valido() {
        assertTrue(ValidacionUtil.esEmailValido("estudiante@utec.edu.uy"));
    }

    @Test
    @DisplayName("Email sin arroba debe fallar")
    void email_sin_arroba() {
        assertFalse(ValidacionUtil.esEmailValido("estudianteutec.edu.uy"));
    }

    @Test
    @DisplayName("Email nulo debe fallar")
    void email_nulo() {
        assertFalse(ValidacionUtil.esEmailValido(null));
    }
}
