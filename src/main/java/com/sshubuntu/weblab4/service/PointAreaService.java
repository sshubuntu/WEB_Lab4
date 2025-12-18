package com.sshubuntu.weblab4.service;

import jakarta.ejb.Stateless;

@Stateless
public class PointAreaService {

    public boolean isHit(double x, double y, double r) {
        if (r == 0) {
            return false;
        }

        double effectiveR = Math.abs(r);


        if (r < 0) {
            return isHitPositive(-x, -y, effectiveR);
        }

        return isHitPositive(x, y, effectiveR);
    }

    private boolean isHitPositive(double x, double y, double r) {
        return inRectangle(x, y, r) || inTriangle(x, y, r) || inQuarterCircle(x, y, r);
    }

    private boolean inRectangle(double x, double y, double r) {
        double halfR = r / 2.0;
        return x >= -r && x <= 0 && y >= -halfR && y <= 0;
    }

    private boolean inTriangle(double x, double y, double r) {
        double halfR = r / 2.0;
        return x >= 0 && x <= halfR && y <= 0 && y >= -halfR && y >= x - halfR;
    }

    private boolean inQuarterCircle(double x, double y, double r) {
        double radius = r / 2.0;
        if (x < 0 || y < 0) {
            return false;
        }
        return x * x + y * y <= radius * radius;
    }
}




