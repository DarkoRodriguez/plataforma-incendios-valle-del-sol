package com.valledelsol.usuarios.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleIllegalArgumentException() {
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Invalid argument"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Invalid argument", body.get("message"));
    }

    @Test
    void testHandleGlobalException() {
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> response = handler.handleGlobalException(
                new RuntimeException("Unexpected error"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Unexpected error", body.get("details"));
    }
}
