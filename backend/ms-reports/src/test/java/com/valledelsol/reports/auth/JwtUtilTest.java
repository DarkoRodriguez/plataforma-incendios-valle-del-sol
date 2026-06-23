package com.valledelsol.reports.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtUtilTest {

    @Test
    void testGetUserIdAndRoleFromValidToken() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        String token = Jwts.builder()
                .setSubject("user1")
                .claim("userId", 25L)
                .claim("role", "ADMINISTRATOR")
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        JwtUtil util = new JwtUtil("http://localhost:0");
        Field publicKeyField = JwtUtil.class.getDeclaredField("publicKey");
        publicKeyField.setAccessible(true);
        publicKeyField.set(util, keyPair.getPublic());

        assertEquals(25L, util.getUserId("Bearer " + token));
        assertEquals("ADMINISTRATOR", util.getRole("Bearer " + token));
    }

    @Test
    void testInvalidTokenReturnsNull() throws Exception {
        JwtUtil util = new JwtUtil("http://localhost:0");
        Field publicKeyField = JwtUtil.class.getDeclaredField("publicKey");
        publicKeyField.setAccessible(true);
        publicKeyField.set(util, null);

        assertNull(util.getUserId("Bearer invalid"));
        assertNull(util.getRole("Bearer invalid"));
    }
}
