package com.valledelsol.usuarios.controller;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwksControllerTest {

    @Test
    void testJwksReturnsKeySet() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(512);
        KeyPair keyPair = generator.generateKeyPair();
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID("users-key-1")
                .build();

        JwksController controller = new JwksController(rsaKey);
        Map<String, Object> jwks = controller.jwks();

        assertNotNull(jwks);
        assertTrue(jwks.containsKey("keys"));
    }
}
