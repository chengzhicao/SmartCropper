//
// Created by qiulinmin on 8/1/17.
//
#include <jni.h>
#include <string>
#include <android_utils.h>
#include <Scanner.h>
#include <android/log.h>
#include "../../Dobby/include/dobby.h"

using namespace std;

static const char *const kClassDocScanner = "me/pqpo/smartcropperlib/SmartCropper";

static struct {
    JNIEnv *env;
    jclass jClassPoint;
    jmethodID jMethodInit;
    jfieldID jFieldIDX;
    jfieldID jFieldIDY;
} gPointInfo;

static void initClassInfo(JNIEnv *env) {
    gPointInfo.env = env;
    gPointInfo.jClassPoint = reinterpret_cast<jclass>(env->NewGlobalRef(
            env->FindClass("android/graphics/Point")));
    gPointInfo.jMethodInit = env->GetMethodID(gPointInfo.jClassPoint, "<init>", "(II)V");
    gPointInfo.jFieldIDX = env->GetFieldID(gPointInfo.jClassPoint, "x", "I");
    gPointInfo.jFieldIDY = env->GetFieldID(gPointInfo.jClassPoint, "y", "I");
}

static jobject createJavaPoint(JNIEnv *env, Point point_) {
    return env->NewObject(gPointInfo.jClassPoint, gPointInfo.jMethodInit, point_.x, point_.y);
}

void hbuuu(int a, const char *s1, const char *s2) {
    __android_log_print(ANDROID_LOG_DEBUG, "ofiweg", "我是原来方法");
}

extern "C"
JNIEXPORT void JNICALL
Java_me_pqpo_smartcropperlib_SmartCropper_nativeScan(JNIEnv *env, jclass type, jobject srcBitmap,
                                                     jobjectArray outPoint_, jboolean canny) {
    if (env->GetArrayLength(outPoint_) != 4) {
        return;
    }
    Mat srcBitmapMat;
    bitmap_to_mat(env, srcBitmap, srcBitmapMat);
//    hbuuu(1, "aa", "bb");
    Mat bgrData(srcBitmapMat.rows, srcBitmapMat.cols, CV_8UC3);
    cvtColor(srcBitmapMat, bgrData, COLOR_RGBA2BGR);
    scanner::Scanner docScanner(bgrData, canny);
    std::vector<Point> scanPoints = docScanner.scanPoint();
    if (scanPoints.size() == 4) {
        for (int i = 0; i < 4; ++i) {
            env->SetObjectArrayElement(outPoint_, i, createJavaPoint(env, scanPoints[i]));
        }
    }
}

static vector<Point> pointsToNative(JNIEnv *env, jobjectArray points_) {
    int arrayLength = env->GetArrayLength(points_);
    vector<Point> result;
    for (int i = 0; i < arrayLength; i++) {
        jobject point_ = env->GetObjectArrayElement(points_, i);
        int pX = env->GetIntField(point_, gPointInfo.jFieldIDX);
        int pY = env->GetIntField(point_, gPointInfo.jFieldIDY);
        result.push_back(Point(pX, pY));
    }
    return result;
}

static void (*orig_Java_me_pqpo_smartcropperlib_SmartCropper_nativeScan)(JNIEnv *env, jclass type,
                                                                         jobject srcBitmap,
                                                                         jobjectArray outPoint_,
                                                                         jboolean canny);

extern "C"
JNIEXPORT void JNICALL
new_Java_me_pqpo_smartcropperlib_SmartCropper_nativeScan(JNIEnv *env, jclass type,
                                                         jobject srcBitmap,
                                                         jobjectArray outPoint_, jboolean canny) {
    __android_log_print(ANDROID_LOG_DEBUG, "ofiweg", "我被hook了");
}

static int (*orig_log_print)(int prio, const char *tag, const char *fmt, ...);

static int my_libtest_log_print(int prio, const char *tag, const char *fmt, ...) {
    va_list ap;
    char buf[1024];
    int r;

    snprintf(buf, sizeof(buf), "[%s] %s", (NULL == tag ? "" : tag), (NULL == fmt ? "" : fmt));

    va_start(ap, fmt);
    r = __android_log_vprint(prio, "Dobby_libtest", buf, ap);
    va_end(ap);
    return r;
}

void (*orig_hbuuu)(const char *s1, int a, const char *s2);

void new_hbuuu(int a, const char *s1, const char *s2) {
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo", "我被hook了..%s..%s..%d", s1, s2, a);
}

void (*orig_parseJ2cPoint)();

void
new_parseJ2cPoint(int a1, char *a2, void *a3, int a4, int a5, int a6, int a7, int a8) {
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo", "我被hook了");
    jobject obj = gPointInfo.env->NewDirectByteBuffer(a3, a8);
//    gPointInfo.env->SetByteArrayRegion(array, 0, 0, len, (jbyte *) buffer);
    jclass jclazz = gPointInfo.env->FindClass("me/pqpo/smartcropperlib/JniCallBack");
    jmethodID jmethodIds = gPointInfo.env->GetMethodID(jclazz, "callBack",
                                                       "(Ljava/nio/ByteBuffer;)V");
    jobject object = gPointInfo.env->AllocObject(jclazz);
    gPointInfo.env->CallVoidMethod(object, jmethodIds, obj);
}

//void
//new_parseJ2cPoint(int a1, const char *a2, const void *a3, int a4, int a5, int a6, int a7, int a8) {
//    __android_log_print(ANDROID_LOG_DEBUG, "iewoo", "我被hook了..%d..%s..%d..%d..%d..%d..%d..%d",
//                        a1, a2, a3, a4, a5, a6, a7, a8);
//}

//void
//new_parseJ2cPoint(int a1, const char *a2, const void *a3, int a4, int a5, int a6, int a7, int a8) {
////    char nn[a8];
////    memcpy(nn, a3, a8);
//    __android_log_print(ANDROID_LOG_DEBUG, "iewoo", "我被hook了");
//    jbyteArray array = gPointInfo.env->NewByteArray(a8);
//    gPointInfo.env->SetByteArrayRegion(array, 0, a8, (jbyte *) a3);
//    jclass jclazz = gPointInfo.env->FindClass("me/pqpo/smartcropperlib/JniCallBack");
//    jmethodID jmethodIds = gPointInfo.env->GetMethodID(jclazz, "callBack2",
//                                                       "([B)V");
//    jobject object = gPointInfo.env->AllocObject(jclazz);
//    gPointInfo.env->CallVoidMethod(object, jmethodIds, array);
//}

__attribute__((constructor)) static void ctor() {
    // Write hook functions here
    __android_log_print(ANDROID_LOG_DEBUG, "ifwoowe", "初始");
    void *address = DobbySymbolResolver(NULL, "_ZN12VcapInstance8setInputEPKcPKviiiii");
    DobbyHook(address, (dobby_dummy_func_t) new_parseJ2cPoint,
              (dobby_dummy_func_t *) &orig_parseJ2cPoint);
}

extern "C"
JNIEXPORT void JNICALL
Java_me_pqpo_smartcropperlib_SmartCropper_nativeCrop(JNIEnv *env, jclass type, jobject srcBitmap,
                                                     jobjectArray points_, jobject outBitmap) {
    std::vector<Point> points = pointsToNative(env, points_);
    if (points.size() != 4) {
        return;
    }
    Point leftTop = points[0];
    Point rightTop = points[1];
    Point rightBottom = points[2];
    Point leftBottom = points[3];

    Mat srcBitmapMat;
    bitmap_to_mat(env, srcBitmap, srcBitmapMat);

    AndroidBitmapInfo outBitmapInfo;
    AndroidBitmap_getInfo(env, outBitmap, &outBitmapInfo);
    Mat dstBitmapMat;
    int newHeight = outBitmapInfo.height;
    int newWidth = outBitmapInfo.width;
    dstBitmapMat = Mat::zeros(newHeight, newWidth, srcBitmapMat.type());

    std::vector<Point2f> srcTriangle;
    std::vector<Point2f> dstTriangle;

    srcTriangle.push_back(Point2f(leftTop.x, leftTop.y));
    srcTriangle.push_back(Point2f(rightTop.x, rightTop.y));
    srcTriangle.push_back(Point2f(leftBottom.x, leftBottom.y));
    srcTriangle.push_back(Point2f(rightBottom.x, rightBottom.y));

    dstTriangle.push_back(Point2f(0, 0));
    dstTriangle.push_back(Point2f(newWidth, 0));
    dstTriangle.push_back(Point2f(0, newHeight));
    dstTriangle.push_back(Point2f(newWidth, newHeight));

    Mat transform = getPerspectiveTransform(srcTriangle, dstTriangle);
    warpPerspective(srcBitmapMat, dstBitmapMat, transform, dstBitmapMat.size());

    mat_to_bitmap(env, dstBitmapMat, outBitmap);
}

//static JNINativeMethod gMethods[] = {
//
//        {
//                "nativeScan",
//                "(Landroid/graphics/Bitmap;[Landroid/graphics/Point;Z)V",
//                (void*)native_scan
//        },
//
//        {
//                "nativeCrop",
//                "(Landroid/graphics/Bitmap;[Landroid/graphics/Point;Landroid/graphics/Bitmap;)V",
//                (void*)native_crop
//        }
//
//};

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_FALSE;
    }
//    jclass classDocScanner = env->FindClass(kClassDocScanner);
//    if(env -> RegisterNatives(classDocScanner, gMethods, sizeof(gMethods)/ sizeof(gMethods[0])) < 0) {
//        return JNI_FALSE;
//    }
    initClassInfo(env);
    return JNI_VERSION_1_4;
}
