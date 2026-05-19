package com.example.ms_mapeo.controller;

import com.example.ms_mapeo.dto.ReporteIncendioDTO;
import com.example.ms_mapeo.service.ReporteIncendioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteIncendioController {

    @Autowired
    private ReporteIncendioService service;

    @PostMapping
    public ResponseEntity<ReporteIncendioDTO> crearReporte(@RequestBody ReporteIncendioDTO dto) {
        return ResponseEntity.ok(service.crearReporte(dto));
    }

    @GetMapping
    public ResponseEntity<List<ReporteIncendioDTO>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }
}
