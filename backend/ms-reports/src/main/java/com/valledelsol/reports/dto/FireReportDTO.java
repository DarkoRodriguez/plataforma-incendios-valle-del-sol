package com.valledelsol.reports.dto;

import lombok.Data;

@Data
public class FireReportDTO {
    private Long id;
    private String description;
    private String type;
    private String status;
    private double latitude;
    private double longitude;
    private String region;
    private String commune;
    private String reportDate;
    private Long userId;
    private String mediaUrl;
}
