package com.example.ms_mapeo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un Reporte de Foco de Incendio en la base de datos PostgreSQL/PostGIS.
 * Almacena la descripción, el tipo de incendio, su estado y su ubicación geográfica como un Point espacial.
 */
@Entity
@Table(name = "reportes_incendio")
@Data
@NoArgsConstructor
public class ReporteIncendio {

    /**
     * Identificador único autogenerado del reporte de incendio.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Descripción textual detallada de la situación del foco de incendio.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Tipo de incendio (ej: FORESTAL, ESTRUCTURAL, etc.).
     */
    @Column(nullable = false, length = 50)
    private String tipo;

    /**
     * Estado operativo actual del foco (ACTIVO, CONTROLADO, EXTINGUIDO).
     */
    @Column(nullable = false, length = 50)
    private String estado;

    /**
     * Identificador del usuario creador del reporte (opcional).
     */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /**
     * Ubicación geográfica espacial almacenada como un punto de coordenadas (Point) usando PostGIS SRID 4326.
     */
    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    /**
     * Marca de tiempo que registra cuándo se creó el reporte.
     */
    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    /**
     * Método ciclo de vida JPA que asigna la fecha y hora actual automáticamente al persistir el objeto.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
