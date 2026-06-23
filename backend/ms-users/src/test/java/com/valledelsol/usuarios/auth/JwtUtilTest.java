package com.valledelsol.usuarios.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
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

    @Test
    void testParseTokenFetchesPublicKeyFromJwksWhenPublicKeyIsNull() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();

        String n = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/jwks", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws java.io.IOException {
                String body = "{\"keys\":[{\"kty\":\"RSA\",\"n\":\"" + n + "\",\"e\":\"" + e + "\"}]}";
                exchange.sendResponseHeaders(200, body.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body.getBytes());
                }
            }
        });
        server.start();

        JwtUtil util = new JwtUtil("http://localhost:" + server.getAddress().getPort() + "/jwks");
        Field publicKeyField = JwtUtil.class.getDeclaredField("publicKey");
        publicKeyField.setAccessible(true);
        publicKeyField.set(util, null);

        String token = Jwts.builder()
                .setSubject("user1")
                .claim("userId", 25L)
                .claim("role", "ADMINISTRATOR")
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertEquals(25L, util.getUserId("Bearer " + token));
        assertEquals("ADMINISTRATOR", util.getRole("Bearer " + token));

        server.stop(0);
    }
}
