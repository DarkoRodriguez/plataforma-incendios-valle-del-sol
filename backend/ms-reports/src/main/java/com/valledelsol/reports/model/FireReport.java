package com.valledelsol.reports.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un reporte de incendio forestal o urbano.
 */
@Entity
@Table(name = "fire_reports")
@Data
@NoArgsConstructor
public class FireReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String commune;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @PrePersist
    protected void onCreate() {
        if (reportDate == null) {
            reportDate = LocalDateTime.now();
        }
    }
}
