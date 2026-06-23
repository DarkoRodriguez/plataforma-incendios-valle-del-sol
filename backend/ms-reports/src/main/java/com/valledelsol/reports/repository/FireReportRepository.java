package com.valledelsol.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.valledelsol.reports.model.FireReport;

@Repository
public interface FireReportRepository extends JpaRepository<FireReport, Long> {

    @Query("SELECT COUNT(f) FROM FireReport f WHERE f.type = :reportType AND LOWER(f.status) IN ('activo', 'active')")
    int getActiveReportsCountByType(@Param("reportType") String reportType);
}
