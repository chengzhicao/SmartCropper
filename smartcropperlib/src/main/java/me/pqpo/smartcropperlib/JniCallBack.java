package me.pqpo.smartcropperlib;

import android.util.Log;

import java.nio.ByteBuffer;

public class JniCallBack {
    public void callBack(ByteBuffer byteBuffer) {
//        float[] b = new float[200];
        byte[] b = new byte[byteBuffer.limit()];
        byteBuffer.rewind();
        for (int i = 0; i < b.length; i++) {
            b[i] = byteBuffer.get();
        }
        byteBuffer.clear();
//        byteBuffer.clear();
//        callBack2(b);
        byte[] before = new byte[200];
        byte[] after = new byte[200];
        System.arraycopy(b, 0, before, 0, 200);
        System.arraycopy(b, b.length - 200, after, 0, 200);
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
