package me.pqpo.smartcropperlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;

import com.wibo.doc.jni.DocDetectApi;
import com.wibo.doc.jni.DocPoint;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.InterpreterFactory;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class ImageDetector {

    private static final String MODEL_FILE = "models/hed_lite_model_quantize.tflite";

    private final int desiredSize = 256;

    protected InterpreterApi tflite;

    private Context context;

    public ImageDetector(Context context) throws IOException {
        this(context, MODEL_FILE);
    }

    private DocDetectApi docDetectApi;

    public ImageDetector(Context context, String modelFile) throws IOException {
        docDetectApi = new DocDetectApi();
        boolean i = docDetectApi.initModel(context, 0, "com.wibo.bigbong.ocr");
        Log.i("joifweg", i + "");
        this.context = context;
        if (TextUtils.isEmpty(modelFile)) {
            modelFile = MODEL_FILE;
        }
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context,
                modelFile);
        InterpreterApi.Options options = new InterpreterApi.Options();
        tflite = new InterpreterFactory().create(
                tfliteModel, options);
    }

    public synchronized Bitmap detectImage(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
                        .build();
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);
//        TensorBuffer probabilityBuffer =
//                TensorBuffer.createFixedSize(new int[]{1, desiredSize * desiredSize}, DataType.FLOAT32);
//        tflite.run(tensorImage.getBuffer(), probabilityBuffer.getBuffer());
//        float[] detect = new Detect().detect(context, tensorImage.getBitmap());
        return Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
    }

    public synchronized Point[] detectImage2(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
//        ImageProcessor imageProcessor =
//                new ImageProcessor.Builder()
//                        .add(new ResizeOp(512, 512, ResizeOp.ResizeMethod.BILINEAR))
//                        .build();
//        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
//        tensorImage.load(bitmap);
//        tensorImage = imageProcessor.process(tensorImage);
        DocPoint[] docPoints = new DocPoint[]{new DocPoint(0, 0), new DocPoint(0, 0), new DocPoint(0, 0), new DocPoint(0, 0)};
        DocPoint[] docPoints1 = docDetectApi.callJniDocDetectAlbum(bitmap, docPoints);
        Log.i("joifweg", docPoints1[0].toString() + " " + docPoints1[1].toString() + " "
                + docPoints1[2].toString() + " " + docPoints1[3].toString() + " ");
        Point[] points = new Point[4];
        points[0] = new Point((int) docPoints1[0].getX(), (int) docPoints1[0].getY());
        points[1] = new Point((int) docPoints1[1].getX(), (int) docPoints1[1].getY());
        points[2] = new Point((int) docPoints1[2].getX(), (int) docPoints1[2].getY());
        points[3] = new Point((int) docPoints1[3].getX(), (int) docPoints1[3].getY());
        return points;
    }

    private Bitmap convertOutputBufferToBitmap(float[] out) {
        if (out == null) {
            return null;
        }
        Bitmap bitmap_out = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[desiredSize * desiredSize];
        for (int i = 0; i < desiredSize * desiredSize; i++) {
            float val = out[i];
            if (val > 0.2) {
                pixels[i] = 0xFFFFFFFF;
            } else {
                pixels[i] = 0xFF000000;
            }
        }
        bitmap_out.setPixels(pixels, 0, desiredSize, 0, 0, desiredSize, desiredSize);
        return bitmap_out;
    }
}
