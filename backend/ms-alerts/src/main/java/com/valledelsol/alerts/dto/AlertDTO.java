package com.valledelsol.alerts.dto;

import lombok.Data;

@Data
public class AlertDTO {
    private Long id;
    private String title;
    private String message;
    private String level;
    private String region;
    private String commune;
    private String createdAt;
}
