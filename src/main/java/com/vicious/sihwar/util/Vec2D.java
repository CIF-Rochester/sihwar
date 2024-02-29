package com.vicious.sihwar.util;

import com.vicious.viciouslib.persistence.storage.aunotamations.Save;

public class Vec2D{
    @Save
    public double x;
    @Save
    public double y;

    public Vec2D(double x, double y){
        this.x = x;
        this.y = y;
    }
}
