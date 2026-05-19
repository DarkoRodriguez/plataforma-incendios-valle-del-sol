package com.example.ms_mapeo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_incendio")
@Data
@NoArgsConstructor
public class ReporteIncendio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 50)
    private String estado;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    @PrePersist
    protected void onCreate() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
    }
}
