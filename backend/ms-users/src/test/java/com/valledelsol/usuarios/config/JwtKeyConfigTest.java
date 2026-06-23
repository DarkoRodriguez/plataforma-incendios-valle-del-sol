package com.valledelsol.usuarios.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtKeyConfigTest {

    @Test
    void testBeansLoadFromTempPemFiles() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(512);
        KeyPair keyPair = generator.generateKeyPair();

        String privatePem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        String publicPem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";

        Path privatePath = Files.createTempFile("private", ".pem");
        Path publicPath = Files.createTempFile("public", ".pem");
        Files.writeString(privatePath, privatePem);
        Files.writeString(publicPath, publicPem);

        JwtKeyConfig config = new JwtKeyConfig();
        RSAPrivateKey privateKey = config.privateKey(privatePath.toString());
        RSAPublicKey publicKey = config.publicKey(publicPath.toString());

        assertNotNull(privateKey);
        assertNotNull(publicKey);
        assertNotNull(config.rsaKey(publicKey, privateKey));
        assertNotNull(config.jwkSource(config.rsaKey(publicKey, privateKey)));
        assertNotNull(config.jwtEncoder(config.jwkSource(config.rsaKey(publicKey, privateKey))));
    }
}
