/*
 * Derived from davemorrissey/subsampling-scale-image-view
 * https://github.com/davemorrissey/subsampling-scale-image-view
 *
 * Original work Copyright 2018 David Morrissey
 * Licensed under the Apache License, Version 2.0.
 *
 * Modified for this project:
 * - Package names were relocated for local integration.
 * - The source was embedded directly into the app module.
 */
package com.example.util.subsampling;


import android.graphics.PointF;

import java.util.List;

class MathUtil {

    static PointF midPoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    static double distance(PointF p1, PointF p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
                * (p1.y - p2.y));
    }

    static double angle(PointF origin, PointF p) {
        return Math.atan2(p.y - origin.y, p.x - origin.x);
    }

    static double squareDistance(PointF p1, PointF p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
    }

    public static double mean(List<Integer> list) {
        double sum = 0;
        for (int i : list) {
            sum += i;
        }
        return sum / list.size();
    }

    public static double sum(double[] d) {
        double sum = 0;
        for (double i : d) {
            sum += i;
        }
        return sum;
    }
}

