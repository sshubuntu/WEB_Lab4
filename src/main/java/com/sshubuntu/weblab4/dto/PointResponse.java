package com.sshubuntu.weblab4.dto;

import com.sshubuntu.weblab4.entity.PointResult;

import java.time.format.DateTimeFormatter;

public class PointResponse {
    private Long id;
    private double x;
    private double y;
    private double r;
    private boolean hit;
    private String creationTime;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static PointResponse from(PointResult result) {
        PointResponse response = new PointResponse();
        response.setId(result.getId());
        response.setX(result.getX());
        response.setY(result.getY());
        response.setR(result.getR());
        response.setHit(result.isHit());
        response.setCreationTime(result.getCreationTime() != null ? FORMATTER.format(result.getCreationTime()) : "");
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}




