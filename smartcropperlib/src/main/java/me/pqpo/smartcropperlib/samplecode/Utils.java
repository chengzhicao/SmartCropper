package me.pqpo.smartcropperlib.samplecode;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Utils {
    /** Memory-map the model file in Assets. */
    public static  MappedByteBuffer mapModelFromAssets(Context context, String assetsName)throws IOException {

        AssetFileDescriptor fileDescriptor=null;
        FileInputStream inputStream=null;
        FileChannel fileChannel=null;
        MappedByteBuffer byteBuffer=null;
        try{
            fileDescriptor = context.getAssets().openFd(assetsName);
            inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            fileChannel = inputStream.getChannel();

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            byteBuffer=fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

        }catch (IOException e){
            Log.e("Assets", "loadModelFile: fail" + e.toString());
        }finally {
            if(fileChannel != null){
                fileChannel.close();
            }
            if(inputStream != null){
                inputStream.close();
            }
            if(fileDescriptor != null){
                fileDescriptor.close();
            }
            return byteBuffer;
        }

    }

    // resized bitmap
    public static void bitmapToFloatArray(Bitmap bitmap, float mean, float std, float inputValue[], int height, int width){

        int intValues[] = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            inputValue[i] = (((val >> 16) & 0xFF) - mean) / std;//R
            inputValue[width * height + i] = (((val >> 8) & 0xFF) - mean) / std;//G
            inputValue[width * height * 2 + i] = ((val & 0xFF) - mean) / std;//B

        }
    }


    public static void bitmapToByteArrayNchw(Bitmap bitmap, byte inputValue[], int height, int width) {

        int intValues[] = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            inputValue[i] = (byte)((val >> 16) & 0xFF);//R
            inputValue[width * height + i] = (byte)((val >> 8) & 0xFF);//G
            inputValue[width * height * 2 + i] = (byte)(val & 0xFF);//B

        }
    }

    // hta is nhwc
    public static void bitmapToByteArrayNhwc(Bitmap bitmap, byte inputValue[]){
        int intValues[] = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            inputValue[i*3] = (byte)((val >> 16) & 0xFF);//R
            inputValue[i*3 + 1] = (byte)((val >> 8) & 0xFF);//G
            inputValue[i*3 + 2] = (byte)(val & 0xFF);//B
        }

    }



    /** Prints top-K labels, to be shown in UI as the results. */
    public static String printTopKLabels(float result[], List<String> mLabelList) {
        PriorityQueue<Map.Entry<String, Float>> sortedLabels =
                new PriorityQueue<>(
                        3,
                        new Comparator<Map.Entry<String, Float>>() {
                            @Override
                            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                                return (o1.getValue()).compareTo(o2.getValue());
                            }
                        });
        for (int i = 0; i < mLabelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(mLabelList.get(i), result[i]));
            if (sortedLabels.size() > 3) {
                sortedLabels.poll();
            }
        }
        String textToShow = "";
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            textToShow = String.format("\n%s: %4.2f", label.getKey(), label.getValue()) + textToShow;
        }
        return textToShow;
    }

    /** Reads label list from Assets. */
    public static   List<String> loadLabelList(Context context, String labelAssetsName) throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(context.getAssets().open(labelAssetsName)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    public static float[] softmax(float[] input, int len) {
        float max =0.0f;
        float[] output = new float[len];
        for(int i=0; i<len; i++) {
            if(input[i] > max) {
                max = input[i];
            }
        }
        float sum = 0.0f;
        for(int i=0; i<len; i++) {
            output[i] = (float)Math.exp(input[i]-max);
            sum += output[i];
        }
        for(int i=0; i<len; i++) {
            output[i] = output[i]/sum;
        }
        return output;
    }
}
