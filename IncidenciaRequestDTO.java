package com.utec.sienep.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Clave Base64 de 64 bytes mínimo para HMAC-SHA512
    private static final String SECRET =
            "c2llbmVwLXV0ZWMtMjAyNi1zZWNyZXQta2V5LXN1cGVyLXNlZ3VyYS1wYXJhLWpzb24td2ViLXRva2Vu";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
    }

    private UserDetails buildUser(String username) {
        return User.builder()
                .username(username)
                .password("hash")
                .authorities("ROLE_ADMIN")
                .build();
    }

    @Test
    @DisplayName("Token generado no es nulo y tiene formato JWT (tres partes)")
    void generar_token_tiene_formato_jwt() {
        String token = jwtUtil.generarToken(buildUser("admin"));
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Username extraído del token coincide con el original")
    void extraer_username_correcto() {
        UserDetails user = buildUser("admin");
        String token = jwtUtil.generarToken(user);
        assertEquals("admin", jwtUtil.extraerUsername(token));
    }

    @Test
    @DisplayName("Token válido pasa la validación")
    void validar_token_valido_retorna_true() {
        UserDetails user = buildUser("admin");
        String token = jwtUtil.generarToken(user);
        assertTrue(jwtUtil.validarToken(token, user));
    }

    @Test
    @DisplayName("Token con username diferente falla la validación")
    void validar_token_username_diferente_retorna_false() {
        UserDetails user1 = buildUser("admin");
        UserDetails user2 = buildUser("otro");
        String token = jwtUtil.generarToken(user1);
        assertFalse(jwtUtil.validarToken(token, user2));
    }

    @Test
    @DisplayName("Token expirado falla la validación")
    void validar_token_expirado_retorna_false() {
        JwtUtil expiredUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredUtil, "secret", SECRET);
        ReflectionTestUtils.setField(expiredUtil, "expirationMs", -1000L); // ya expiró

        UserDetails user = buildUser("admin");
        String token = expiredUtil.generarToken(user);
        assertFalse(expiredUtil.validarToken(token, user));
    }
}
