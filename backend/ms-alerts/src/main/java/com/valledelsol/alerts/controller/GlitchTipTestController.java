package com.valledelsol.alerts.controller;
import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class GlitchTipTestController {

    // Prueba 1: Captura automática por Spring Boot (Excepción no controlada)
    @GetMapping("/error-automatico")
    public String forzarErrorAutomatico() {
        throw new RuntimeException("¡GlitchTip Test: Error automático en Spring Boot!");
    }

    // Prueba 2: Captura manual mediante bloques Try-Catch
    @GetMapping("/error-manual")
    public String forzarErrorManual() {
        try {
            int resultado = 10 / 0; // Provoca un ArithmeticException
        } catch (Exception e) {
            // Enviamos manualmente la excepción a GlitchTip
            Sentry.captureException(e);
            return "Error capturado manualmente y enviado a GlitchTip.";
        }
        return "Esto no debería alcanzarse.";
    }
}