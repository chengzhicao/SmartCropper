package com.wibo.doc.jni;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes2.dex */
public class DocDetectApi {
    public static String TAG = "DocDetectApi";
    public static String get_version = "doc_aar_v1.5.1.0_v7";
//    public static String mModelAssetsNamePreview = "v5.vaimlite";
//    public static String mModelAssetsNamePreviewDet = "yolo_fp16.vaimlite";
    public static String mModelAssetsNameshot = "v5.vaimlite";
    public static String mModelAssetsNameshotDet = "yolo_fp16.vaimlite";
    private boolean isInitSuccess = false;
    private boolean onDetect = false;
    private boolean isQcom = false;

    /* JADX WARN: Code restructure failed: missing block: B:37:0x0073, code lost:
        if (r6 == null) goto L72;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v5, types: [java.io.BufferedReader] */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* JADX WARN: Type inference failed for: r4v8 */
    /* JADX WARN: Type inference failed for: r4v9, types: [java.lang.String] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */

    private byte[] toByteArray(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[1024];
        while (true) {
            try {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            } catch (IOException unused) {
                Log.e(TAG, "toByteArray: error");
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public synchronized DocPoint[] callJniDocDetectAlbum(Bitmap bitmap, DocPoint[] docPointArr) {
        Object[] objArr;
        objArr = null;
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                objArr = DocDetect.runDetect(bitmap, docPointArr, 1);
            }
        }
        return DocDetect.adjustPoints((DocPoint[]) objArr, bitmap.getWidth(), bitmap.getHeight(), 0);
    }

    public synchronized DocPoint[] callJniDocDetectSync(byte[] bArr, int i2, int i3, int i4, boolean z) {
        Object[] objArr;
        objArr = null;
        if (bArr != null) {
            if (this.isInitSuccess) {
                objArr = DocDetect.docDetectSync(bArr, i2, i3, i4, z, 0);
            }
        }
        return DocDetect.adjustPoints((DocPoint[]) objArr, i2, i3, i4);
    }

    public Bitmap callJniDocRectifyAlbum(Bitmap bitmap, DocPoint[] docPointArr) {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        if (docPointArr == null || bitmap == null || bitmap.isRecycled() || docPointArr.length != 4 || config == null) {
            return null;
        }
        Object runDocRectify = DocDetect.runDocRectify(bitmap, docPointArr, config);
        if (runDocRectify instanceof Bitmap) {
            return (Bitmap) runDocRectify;
        }
        return null;
    }

    public Bitmap callJniDocResize(Bitmap bitmap, int i2) {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        if (bitmap == null || bitmap.isRecycled() || config == null) {
            return null;
        }
        Object Resize = DocDetect.Resize(bitmap, i2, config);
        if (Resize instanceof Bitmap) {
            return (Bitmap) Resize;
        }
        return null;
    }

    public Bitmap callJniJpeg2Bitmap(byte[] bArr, int i2, int i3) {
        if (bArr == null || !this.isInitSuccess) {
            return null;
        }
        return DocDetect.doJpeg2Bitmap(bArr, i2, i3);
    }

    public int getRunTime() {
        return this.isQcom ? 1 : 0;
    }

    /* JADX WARN: Removed duplicated region for block: B:47:0x009e A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00a9 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean initModel(android.content.Context r17, int r18, java.lang.String r19) {
        java.io.InputStream r6;
        getRunTime();
        android.content.res.AssetManager r0 = r17.getAssets();
        java.io.InputStream r4 = null;
        boolean r5 = false;
//        try {
//            r6 = r0.open(com.wibo.doc.jni.DocDetectApi.mModelAssetsNamePreview);
//        } catch (java.io.IOException unused) {
//            r6 = null;
//        }
        try {
//            boolean r7 = com.wibo.doc.jni.DocDetect.initDocDetectModel(toByteArray(r6), 1, false, r19, 0, toByteArray(r0.open(com.wibo.doc.jni.DocDetectApi.mModelAssetsNamePreviewDet)));
//            if (r7) {
//                isInitSuccess = true;
//            }
            r4 = r0.open(com.wibo.doc.jni.DocDetectApi.mModelAssetsNameshot);
            boolean r1 = com.wibo.doc.jni.DocDetect.initDocModel2(toByteArray(r4), 0, false, "", 1, new byte[]{});
            if (r1) {
                isInitSuccess = true;
            }
//            if (r7) {
//                r5 = true;
//            }
//            if (r6 != null) {
//                try {
//                    r6.close();
//                } catch (java.io.IOException unused) {
//                    android.util.Log.e(com.wibo.doc.jni.DocDetectApi.TAG, "initDocModel: fail, model stream close");
//                }
//            }
        } catch (Exception e) {

        }
        return isInitSuccess;
    }

    public boolean releaseDocModel() {
        String str = TAG;
        if (this.onDetect) {
            Log.e(str, "releaseDocModel: docdetect is doing, can not release now");
            return false;
        }
        DocDetect.releaseDocModel(0);
        DocDetect.releaseDocModel(1);
        return true;
    }
}