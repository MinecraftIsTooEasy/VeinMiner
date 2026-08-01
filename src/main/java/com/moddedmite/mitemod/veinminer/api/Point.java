package com.moddedmite.mitemod.veinminer.api;

public class Point {
    private int x;
    private int y;
    private int z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getZ() { return this.z; }

    @Override
    public int hashCode() {
        int hash = 5;
        hash += (13 * this.x);
        hash += (19 * this.y);
        hash += (31 * this.z);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Point)) return false;
        Point otherPoint = (Point) other;
        return this.x == otherPoint.x && this.y == otherPoint.y && this.z == otherPoint.z;
    }

    public int distanceFrom(Point target) {
        return distanceFrom(target.x, target.y, target.z);
    }

    public int distanceFrom(int x, int y, int z) {
        int distanceX = this.x - x;
        int distanceY = this.y - y;
        int distanceZ = this.z - z;
        return distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
    }

    public boolean isWithinRange(Point target, int range) {
        return isWithinRange(target.x, target.y, target.z, range);
    }

    public boolean isWithinRange(int x, int y, int z, int range) {
        return distanceFrom(x, y, z) <= (range * range);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }
}
