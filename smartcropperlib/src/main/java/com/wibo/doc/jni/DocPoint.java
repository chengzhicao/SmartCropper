package com.wibo.doc.jni;

public class DocPoint {
    private float x;
    private float y;

    public DocPoint(float f2, float f3) {
        this.x = f2;
        this.y = f3;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public void setX(float f2) {
        this.x = f2;
    }

    public void setY(float f2) {
        this.y = f2;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
