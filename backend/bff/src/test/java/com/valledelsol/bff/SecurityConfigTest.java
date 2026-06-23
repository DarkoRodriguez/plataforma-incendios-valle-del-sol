package com.valledelsol.bff;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void testSecurityFilterChainBuildsWithHttpSecurity() throws Exception {
        ObjectPostProcessor<Object> postProcessor = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };
        AuthenticationManagerBuilder authBuilder = new AuthenticationManagerBuilder(postProcessor);

        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("jwtDecoder", mock(JwtDecoder.class));
        context.refresh();

        HttpSecurity http = new HttpSecurity(postProcessor, authBuilder, Map.of());
        http.setSharedObject(ApplicationContext.class, context);

        SecurityFilterChain chain = new SecurityConfig().securityFilterChain(http);

        assertNotNull(chain);
    }
}
