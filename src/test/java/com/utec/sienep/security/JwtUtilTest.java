package com.utec.sienep.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import javax.crypto.SecretKey;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Inyectar el secreto via reflexión (simula el @Value)
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "dGVzdFNlY3JldEtleVBhcmFKd3RVdGVjU2llbmVwMjAyNlByb3llY3Rv");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
    }

    private UserDetails userDetails(String username) {
        return User.withUsername(username).password("irrelevante").roles("USER").build();
    }

    @Test
    void generar_token_retorna_string_no_nulo() {
        String token = jwtUtil.generarToken(userDetails("admin"));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extraer_username_retorna_username_correcto() {
        UserDetails ud = userDetails("admin");
        String token = jwtUtil.generarToken(ud);
        assertEquals("admin", jwtUtil.extraerUsername(token));
    }

    @Test
    void validar_token_valido_retorna_true() {
        UserDetails ud = userDetails("admin");
        String token = jwtUtil.generarToken(ud);
        assertTrue(jwtUtil.validarToken(token, ud));
    }

    @Test
    void validar_token_usuario_incorrecto_retorna_false() {
        String token = jwtUtil.generarToken(userDetails("admin"));
        UserDetails otroUser = userDetails("otro");
        assertFalse(jwtUtil.validarToken(token, otroUser));
    }

    @Test
    void validar_token_expirado_retorna_false() {
        // Construir token ya expirado usando el mismo secreto
        String secret = (String) ReflectionTestUtils.getField(jwtUtil, "secret");
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        String tokenExpirado = Jwts.builder().subject("usuario_test").issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key)
                .compact();

        UserDetails ud = userDetails("usuario_test");
        assertFalse(jwtUtil.validarToken(tokenExpirado, ud));
    }
}
