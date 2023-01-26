package com.wibo.doc.jni;

import android.graphics.Bitmap;
import android.util.Log;

public class DocDetect {
    public static String TAG = "DocDetect";

    static {

    }

    public static native Object Resize(Object obj, int i2, Object obj2);

    public static DocPoint[] adjustPoints(DocPoint[] docPointArr, int i2, int i3, int i4) {
        if (docPointArr == null) {
            Log.e(TAG, "runDocDetect: invalid point return");
            float f2 = i2;
            float f3 = i3;
            return new DocPoint[]{new DocPoint(0.0f, 0.0f), new DocPoint(f2, 0.0f), new DocPoint(f2, f3), new DocPoint(0.0f, f3)};
        }
        if (i4 % 180 != 0) {
            i3 = i2;
            i2 = i3;
        }
        for (DocPoint docPoint : docPointArr) {
            if (docPoint.getX() < 0.0f) {
                docPoint.setX(0.0f);
            }
            if (docPoint.getY() < 0.0f) {
                docPoint.setY(0.0f);
            }
            float f4 = i2;
            if (docPoint.getX() > f4) {
                docPoint.setX(f4);
            }
            float f5 = i3;
            if (docPoint.getY() > f5) {
                docPoint.setY(f5);
            }
        }
        return docPointArr;
    }

    public static native Bitmap doJpeg2Bitmap(byte[] bArr, int i2, int i3);

    public static native Object[] docDetectSync(byte[] bArr, int i2, int i3, int i4, boolean z, int i5);

    public static native boolean initDocDetectModel(byte[] bArr, int i2, boolean z, String str, int i3, byte[] bArr2);

    public static native void releaseDocModel(int i2);

    public static native Object[] runDocDetect(Object obj, Object[] objArr, int i2);

    public static native Object runDocRectify(Object obj, Object[] objArr, Object obj2);
}
