package com.valledelsol.bff;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    @Test
    void testCorsMappingsAdded() {
        TestCorsRegistry registry = new TestCorsRegistry();
        new CorsConfig().addCorsMappings(registry);

        Map<String, CorsConfiguration> configs = registry.getConfigurations();
        assertEquals(1, configs.size());
        assertTrue(configs.containsKey("/**"));
        CorsConfiguration configuration = configs.get("/**");
        assertTrue(configuration.getAllowedOrigins().contains("*"));
        assertTrue(configuration.getAllowedMethods().contains("GET"));
    }

    private static class TestCorsRegistry extends CorsRegistry {
        public Map<String, CorsConfiguration> getConfigurations() {
            return getCorsConfigurations();
        }
    }
}
