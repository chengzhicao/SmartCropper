package me.pqpo.smartcropperlib.samplecode;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.vivo.vcap.VcapInstance;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;




public class VcapImageClassifierQuant {
    private static final String TAG = "VcapImageClassifierQuant";
    VcapInstance mVcapNet = null;
    private VcapInstance.Runtime mRuntime = VcapInstance.Runtime.QCOM_NPU;

    private int mNumClasses = 1001;
    private int mInputBatch = 1;
    private int mInputChanels = 3;
    private int mInputSizeH = 224;
    private int mInputSizeW = 224;
    private int mInputByteSize = 0;

    private float mImageMean = 127.5f;
    private float mImageStd = 127.5f;
    private String mInputNode = "input";
    private String mOutputNode = "output_MobilenetV1/Predictions/Softmax";//"MobilenetV1/Logits/Conv2d_1c_1x1/Conv2D" "output_MobilenetV1/Predictions/Softmax"
    private String mModelAssetsName = "";
    private String mLabelAssetsName = "labels.txt";
    private byte[] mInputarr = null;
    private float[] mNetOutputData = null;

    private List<String> mLabelList;
    String mModelPath = "";
    String mShowRuntime = "";


    //1.initModel
    public  boolean initModel(Context context) {

        Log.d(TAG, "initModel begin runtime:"+ mRuntime);
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
        mInputarr = new byte[mInputSizeH * mInputSizeW *mInputChanels];
        mNetOutputData = new float[mNumClasses];
        mInputByteSize = mInputSizeH * mInputSizeW *mInputChanels;
        boolean ret = getNetwork(context);
        if (!ret) {
            Log.e(TAG, "initModel fail");
            return false;
        }
        Log.d(TAG, "initModel success");
        return true;
    }

    private boolean getNetwork(Context context){
        MappedByteBuffer modelMapBuffer = null;
        try {
            modelMapBuffer = Utils.mapModelFromAssets(context, mModelAssetsName);
        } catch (IOException e) {
            Log.e(TAG, "loadModelDynamic: Map model buffer fail");
            return false;
        }
        //String modelPath = "/sdcard/vcaptest/mobilenet_v1_1.0_224_quant_hta_uint8_nchw.vaimlite";
        String dspLibPath = context.getApplicationInfo().nativeLibraryDir;
        String npuLibPath = context.getApplicationInfo().nativeLibraryDir;
        mVcapNet = new VcapInstance();
        if (mVcapNet == null) return false;
        mVcapNet = mVcapNet.setRuntime(mRuntime)
                .setModelFile(modelMapBuffer)
                .setVcapLogLevel(VcapInstance.VcapLogLevel.VCAP_LOG_LEVEL_VERBOSE)
                .setNpuLibPath(npuLibPath);
        if(mRuntime == VcapInstance.Runtime.QCOM_DSP) {
            mVcapNet = mVcapNet.setDspLibPath(dspLibPath);
        }
        if(mRuntime == VcapInstance.Runtime.QCOM_NPU) {
            mVcapNet = mVcapNet.setNpuLibPath(npuLibPath);
        }
        return mVcapNet.build();
    }



    //2. runModel
    public String runModel(Bitmap bitmap){
        Log.d(TAG, "runModel: ");

        // Preprocess input data
        if (mRuntime == VcapInstance.Runtime.NEON) {
            Utils.bitmapToByteArrayNchw(bitmap, mInputarr, mInputSizeH, mInputSizeW);
        } else {
            Utils.bitmapToByteArrayNhwc(bitmap, mInputarr);
//            Bitmap tmpP = BitmapFactory.decodeFile("/sdcard/goldfish.jpg");
//            Bitmap resizeP = Bitmap.createScaledBitmap(tmpP, 224, 224, false);
//            Utils.bitmapToByteArrayNhwc(resizeP, mInputarr);
        }

        //set input data to input tensor
        long startTime = System.nanoTime();
        mVcapNet.setInput(mInputNode, mInputarr, mInputBatch, mInputChanels, mInputSizeH, mInputSizeW, mInputByteSize);
        mVcapNet.forward();// excute network
        //get output tensor data
        mVcapNet.getOutput(mOutputNode, mNetOutputData);
        long endTime = System.nanoTime();
        String textToShow;
        if (mRuntime == VcapInstance.Runtime.QCOM_NPU) {
            float result[] = Utils.softmax(mNetOutputData, mNumClasses);
            textToShow = Utils.printTopKLabels(result, mLabelList);
        } else {
            textToShow = Utils.printTopKLabels(mNetOutputData, mLabelList);
        }

        String tmpStr = Float.toString((endTime - startTime)/1000000.0f);
        if ( tmpStr.length() > 5) tmpStr = tmpStr.substring(0, 4);
        tmpStr = mShowRuntime + tmpStr;
        textToShow = tmpStr + "ms" + textToShow;
        Log.d(TAG, "getNetwork "+ mRuntime + " RUN time:" + tmpStr);
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
                mOutputNode = "MobilenetV1/Predictions/Softmax";
                //mModelPath = "/sdcard/vcaptest/mobilenet_v1_cpu_uint8.vaim";
                mModelAssetsName = "mobilenet_v1_cpu_uint8.vaim";
                mShowRuntime = "NEON_QUANT: ";
                break;
             case 4:
                 mRuntime = VcapInstance.Runtime.QCOM_DSP;
                 mOutputNode = "output_MobilenetV1/Predictions/Softmax";
                 //mModelPath = "/sdcard/vcaptest/mobilenet_v1_dsp.bin";
                 mModelAssetsName = "mobilenet_v1_dsp.bin";
                 mShowRuntime = "QCOM_DSP: ";
                 break;
            case 5:
                mRuntime = VcapInstance.Runtime.QCOM_NPU;
                mOutputNode = "MobilenetV1/Logits/Conv2d_1c_1x1/Conv2D";
                //mModelPath = "/sdcard/vcaptest/mobilenet_v1_npu.vaim";
                mModelAssetsName = "mobilenet_v1_npu.vaim";
                mShowRuntime = "QCOM_NPU: ";
                break;
            default:
                mRuntime = VcapInstance.Runtime.NEON;
                mOutputNode = "MobilenetV1/Predictions/Softmax";
                //mModelPath = "/sdcard/vcaptest/mobilenet_v1_cpu_uint8.vaim";
                mModelAssetsName = "mobilenet_v1_cpu_uint8.vaim";
                mShowRuntime = "NEON_QUANT: ";
        }
    }

}
