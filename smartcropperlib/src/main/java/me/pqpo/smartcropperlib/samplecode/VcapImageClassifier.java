package me.pqpo.smartcropperlib.samplecode;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.vivo.vcap.VcapInstance;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;


public class VcapImageClassifier {
    private static final String TAG = "VcapImageClassifier";
    VcapInstance mVcapNet = null;
    private VcapInstance.Runtime mRuntime = VcapInstance.Runtime.NEON;

    private int mNumClasses = 1001;
    private int mInputBatch = 1;
    private int mInputChanels = 3;
    private int mInputSizeH = 224;
    private int mInputSizeW = 224;
    private int mInputByteSize = 0;

    private float mImageMean = 127.5f;
    private float mImageStd = 127.5f;
    private String mInputNode = "input";
    private String mOutputNode = "MobilenetV1/Predictions/Softmax";
    private String mModelAssetsName = "mobilenet_v1_float.vaim";
    private String mLabelAssetsName = "labels.txt";
    private float[] mInputarr = null;
    private float[] mNetOutputData = null;

    private List<String> mLabelList;
    String mModelPath = "";
    String mShowRuntime = "";

    //1.initModel
    public  boolean initModel(Context context) {

        Log.d(TAG, "initModel begin");
        if(mVcapNet != null){
            Log.d(TAG, "model has already inited!");
            return true;
        }
        try {
            mLabelList = Utils.loadLabelList(context, mLabelAssetsName);
        } catch (Exception e ){
            Log.e(TAG, "load label fail!");
            return false;
        }
        mInputarr = new float[mInputSizeH * mInputSizeW *mInputChanels];
        mNetOutputData = new float[mNumClasses];
        mInputByteSize = mInputSizeH * mInputSizeW *mInputChanels * 4;
        boolean ret = getNetwork(context);
        if (!ret) {
            Log.e(TAG, "initModel fail");
            return false;
        }
        Log.d(TAG, "initModel success");
        return true;
    }

    private boolean getNetwork(Context context){
        String gpuCachePath = context.getApplicationContext().getExternalCacheDir().getPath() + "/gpubinary.bin";
        MappedByteBuffer modelMapBuffer = null;
        try {
            modelMapBuffer = Utils.mapModelFromAssets(context, mModelAssetsName);
        } catch (IOException e) {
            Log.e(TAG, "loadModelDynamic: Map model buffer fail");
            return false;
        }
        mVcapNet = new VcapInstance();
        if (mVcapNet == null) return false;
        mVcapNet = mVcapNet.setRuntime(mRuntime)
                .setModelFile(modelMapBuffer)
                .setGpuCachePath(gpuCachePath);
        return mVcapNet.build();
    }



    //2. runModel
    public String runModel(Bitmap bitmap){
        Log.d(TAG, "runModel: ");

        // Preprocess input data
        Utils.bitmapToFloatArray(bitmap, mImageMean, mImageStd, mInputarr, mInputSizeH, mInputSizeW);

        //set input data to input tensor
        long startTime = System.nanoTime();
        mVcapNet.setInput(mInputNode, mInputarr, mInputBatch, mInputChanels, mInputSizeH, mInputSizeW, mInputByteSize);
        mVcapNet.forward();// excute network
        //get output tensor data
        mVcapNet.getOutput(mOutputNode, mNetOutputData);
        long endTime = System.nanoTime();
        //PostProcess output
        // show the results.
        String textToShow = Utils.printTopKLabels(mNetOutputData, mLabelList);
        String tmpStr = Float.toString((endTime - startTime)/1000000.0f);
        if ( tmpStr.length() > 5) tmpStr = tmpStr.substring(0, 4);
        tmpStr = mShowRuntime + tmpStr;
        textToShow = tmpStr + "ms" + textToShow;
        return textToShow;
    }
    //3. releaseModel
    public void releaseModel(){
        Log.d(TAG, "releaseModel: ");
        mVcapNet.release();
    }


    public int getImageSizeX() {
        return mInputSizeW;
    }

    public int getImageSizeY() {
        return mInputSizeH;
    }

    public void setRuntime(int runtime) {
        switch (runtime) {
            case 0:
                mRuntime = VcapInstance.Runtime.NEON;
                //mModelPath = "/sdcard/vcaptest/mobilenet_v1_float.vaim";
                mModelAssetsName = "mobilenet_v1_float.vaim";
                mShowRuntime = "CPU_FLOAT: ";
                break;
            case 1:
                mRuntime = VcapInstance.Runtime.OPENCL;
                //mModelPath = "/sdcard/vcaptest/mobilenet_v1_float.vaim";
                mModelAssetsName = "mobilenet_v1_float.vaim";
                mShowRuntime = "OPENCL: ";
                break;

            default:
                mRuntime = VcapInstance.Runtime.NEON;
                //mModelPath = "/sdcard/vcaptest/mobilenet_v1_float.vaim";
                mModelAssetsName = "mobilenet_v1_float.vaim";
                mShowRuntime = "OPENCL: ";
        }
    }
}
