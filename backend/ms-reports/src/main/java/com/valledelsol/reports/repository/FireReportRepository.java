package com.valledelsol.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.valledelsol.reports.model.FireReport;

@Repository
public interface FireReportRepository extends JpaRepository<FireReport, Long> {

    @Procedure(procedureName = "get_active_reports_count_by_type")
    int getActiveReportsCountByType(String reportType);
}
