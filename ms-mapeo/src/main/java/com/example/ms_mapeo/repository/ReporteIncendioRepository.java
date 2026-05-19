package com.example.ms_mapeo.repository;

import com.example.ms_mapeo.model.ReporteIncendio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReporteIncendioRepository extends JpaRepository<ReporteIncendio, Long> {
}
