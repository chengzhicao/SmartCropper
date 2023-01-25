package me.pqpo.smartcropperlib;

import android.util.Log;

import java.nio.ByteBuffer;

public class JniCallBack {
    public void callBack(ByteBuffer byteBuffer) {
        float[] f = new float[byteBuffer.limit() / 4];
//        byte[] b = new byte[byteBuffer.limit()];
        byteBuffer.rewind();
        for (int i = 0; i < f.length; i++) {
            f[i] = byteBuffer.getFloat();
        }
//        byteBuffer.clear();
//        callBack2(b);
        Log.i("Joifweg", "oowow");
    }

    public void callBack2(byte[] bytes) {
        byte[] bb = new byte[1000];
        for (int i = 0; i < bb.length; i++) {
            bb[i] = bytes[i];
        }
        Log.i("Joifweg", "oowow");
    }
}
