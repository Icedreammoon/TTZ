package com.Icedreammoon.TouhouHisoutensoku.utils;

import org.joml.Quaternionf;

public class TTZMathUtil {
    public static Quaternionf quatFromRotationXYZ(float x, float y, float z, boolean degrees) {
        if (degrees) {
            x*=(float)Math.PI/180;
            y*=(float)Math.PI/180;
            z*=(float)Math.PI/180;
        }
        return(new Quaternionf()).rotationXYZ(x, y, z);
    }
}
