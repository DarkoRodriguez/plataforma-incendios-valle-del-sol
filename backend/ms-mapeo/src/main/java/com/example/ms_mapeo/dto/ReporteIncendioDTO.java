package com.example.ms_mapeo.dto;

import lombok.Data;

@Data
public class ReporteIncendioDTO {
    private Long id;
    private String descripcion;
    private String tipo;
    private String estado;
    private double latitud;
    private double longitud;
    private String fechaReporte;
    private Long usuarioId;
}
