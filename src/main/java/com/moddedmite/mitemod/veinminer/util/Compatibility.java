package com.moddedmite.mitemod.veinminer.util;

import com.moddedmite.mitemod.veinminer.api.Point;

public class Compatibility {
    public static Point getPoint(int x, int y, int z) {
        return new Point(x, y, z);
    }
}
