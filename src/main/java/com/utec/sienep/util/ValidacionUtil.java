package com.utec.sienep.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

/**
 * Utilidades de validación del dominio SIENEP.
 * Contiene la lógica portada del sistema anterior (aplicación de consola 2025).
 */
@Component
public class ValidacionUtil {

    /**
     * Valida una cédula de identidad uruguaya mediante el algoritmo del dígito verificador.
     * La cédula se normaliza: se rellena con ceros a la izquierda si tiene 7 dígitos.
     *
     * @param cedula cadena con la cédula (7 u 8 dígitos, sin puntos ni guiones)
     * @return true si la cédula es válida según el algoritmo oficial
     */
    public static boolean validarCedulaUruguaya(String cedula) {
        if (cedula == null || cedula.isBlank()) {
            return false;
        }

        // Limpiar puntos y guiones por si acaso
        cedula = cedula.replaceAll("[.\\-]", "").trim();

        // Aceptar 7 u 8 dígitos; si son 7 rellenar con cero a la izquierda
        if (cedula.length() == 7) {
            cedula = "0" + cedula;
        }

        if (cedula.length() != 8) {
            return false;
        }

        int[] pesos = {2, 9, 8, 7, 6, 3, 4};
        int suma = 0;

        for (int i = 0; i < 7; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));
            if (digito < 0 || digito > 9) {
                return false;
            }
            suma += digito * pesos[i];
        }

        int resto = suma % 10;
        int digitoVerificador = (resto == 0) ? 0 : 10 - resto;
        int ultimoDigito = Character.getNumericValue(cedula.charAt(7));

        return digitoVerificador == ultimoDigito;
    }

    /**
     * Verifica que una persona tenga al menos 18 años en base a su fecha de nacimiento.
     *
     * @param fechaNacimiento fecha de nacimiento
     * @return true si la persona es mayor de edad (>= 18 años)
     */
    public static boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return false;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }

    /**
     * Valida formato de correo electrónico.
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Verifica que un texto contenga solo letras, espacios, apóstrofes y guiones.
     */
    public static boolean esSoloLetras(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        return texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s'\\-]+$");
    }
}
