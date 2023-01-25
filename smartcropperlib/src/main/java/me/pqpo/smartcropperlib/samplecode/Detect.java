package me.pqpo.smartcropperlib.samplecode;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.vivo.vcap.VcapInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Detect {
    private int size = 256;

    public float[] detect(Context context, Bitmap bitmap) {
        VcapInstance mVcapNet = new VcapInstance();
        System.loadLibrary("smart_cropper");
        String modelPath = "v5.vaimlite";
        VcapInstance.Runtime runtime = VcapInstance.Runtime.NEON;
        try {
            mVcapNet = mVcapNet.setRuntime(runtime)
                    .setModelFile(Utils.mapModelFromAssets(context,
                            modelPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mVcapNet.build();
        float[] mInputarr = new float[size * size * 3];
        Utils.bitmapToFloatArray(bitmap, 0f, 255f, mInputarr, size, size);
        //tensor
        int mInputByteSize = size * size * 3 * 4;
        mVcapNet.setInput("hed_input", jfoew(context), 1, 3, size, size,
                mInputByteSize);
        mVcapNet.forward();
        float[] mNetOutputData = new float[size * size];
        mVcapNet.getOutput("hed/dsn_fuse/conv2d/Conv2D", mNetOutputData);
        Log.i("Joifweg", mNetOutputData.length + "");
//        float result[] = Utils.softmax(mNetOutputData, 256 * 256);
        return mNetOutputData;
    }

    private byte[] jfoew(Context context) {
        try {
            InputStream is = context.getAssets().open("input.txt");
            String text = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));
            String[] s = text.split(",");
            byte[] f = new byte[s.length];
            for (int i = 0, len = s.length; i < len; i++) {
                f[i] = Byte.parseByte(s[i]);
            }
            return f;
        } catch (Exception e) {

        }
        return null;
    }
}
