//
// Created by qiulinmin on 8/1/17.
//
#include <jni.h>
#include <string>
#include <android_utils.h>
#include <Scanner.h>
#include <android/log.h>
#include "../../Dobby/include/dobby.h"
#include "vcap_instance.h"
#include <dlfcn.h>

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

extern "C"
JNIEXPORT void JNICALL
Java_me_pqpo_smartcropperlib_SmartCropper_nativeScan(JNIEnv
                                                     *env,
                                                     jclass type, jobject
                                                     srcBitmap,
                                                     jobjectArray outPoint_, jboolean
                                                     canny) {
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

void callJava(const void *a3, int a8) {
    jobject obj = gPointInfo.env->NewDirectByteBuffer(const_cast<void *>(a3), a8);
    jclass jclazz = gPointInfo.env->FindClass("me/pqpo/smartcropperlib/JniCallBack");
    jmethodID jmethodIds = gPointInfo.env->GetMethodID(jclazz, "callBack",
                                                       "(Ljava/nio/ByteBuffer;)V");
    jobject object = gPointInfo.env->AllocObject(jclazz);
    gPointInfo.env->CallVoidMethod(object, jmethodIds, obj);
}

int (*orig_getOut)(void *a1, const char *a2, void *a3);

int new_getOut(void *a1, const char *a2, void *a3) {
    int r = orig_getOut(a1, a2, a3);
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo", "getOutPut被hook了..%p..%s..%p", a1, a2, a3);
    callJava(a3, 256 * 256 * 4);
    return r;
}

int
(*orig_setInput)(void *a1, const char *a2, const void *a3, int a4, int a5, int a6, int a7, int a8);

int
new_setInput(void *a1, const char *a2, const void *a3, int a4, int a5, int a6, int a7, int a8) {
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo",
                        "setInput被hook了..%d..%s..%d..%d..%d..%d..%d..%d",
                        a1, a2, a3, a4, a5, a6, a7, a8);
    callJava(a3, a8);
    return orig_setInput(a1, a2, a3, a4, a5, a6, a7, a8);
}

int
(*orig_initNet)(void *a1, char *a2, int a3, int a4, char *a5, char *a6);

int
new_initNet(void *a1, char *a2, int a3, int a4, char *a5, char *a6) {
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo",
                        "initNet被hook了..%p..%s..%d..%d..%s..%s",
                        a1, a2, a3, a4, a5, a6);
    return orig_initNet(a1, a2, a3, a4, a5, a6);
}

int
(*orig_createNeural)(VcapInstance *a1, vcap::BuildConfig *a2);

int
new_createNeural(VcapInstance *a1, vcap::BuildConfig *a2) {
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo",
                        "createNeural被hook了..%p..%p..%s",
                        a1, a2, a2->output_format);
    return orig_createNeural(a1, a2);
}

__attribute__((constructor)) static void ctor() {
    // Write hook functions here
    __android_log_print(ANDROID_LOG_DEBUG, "ifwoowe", "初始");
//    void *address = DobbySymbolResolver(NULL, "_ZN14VcapdocScanner9netExcuteEv");
//    DobbyHook(address, (dobby_dummy_func_t) new_parseJ2cPoint,
//              (dobby_dummy_func_t *) &orig_parseJ2cPoint);
//
//    DobbyHook(DobbySymbolResolver(NULL, "_ZN12VcapInstance9getOutputEPKcPv"),
//              (dobby_dummy_func_t) new_getOut,
//              (dobby_dummy_func_t *) &orig_getOut);
//    DobbyHook(DobbySymbolResolver(NULL, "_ZN12VcapInstance8setInputEPKcPKviiiii"),
//              (dobby_dummy_func_t) new_setInput,
//              (dobby_dummy_func_t *) &orig_setInput);
//    DobbyHook(DobbySymbolResolver(NULL, "_ZN14VcapdocScanner17initNetWithBufferEPciiPKcS0_"),
//              (dobby_dummy_func_t) new_initNet,
//              (dobby_dummy_func_t *) &orig_initNet);
//    DobbyHook(DobbySymbolResolver(NULL,
//                                  "_ZN12VcapInstance19createNeuralNetworkERN4vcap15VcapBuildConfigE"),
//              (dobby_dummy_func_t) new_createNeural,
//              (dobby_dummy_func_t *) &orig_createNeural);
}

extern "C" JNIEXPORT void JNICALL
Java_me_pqpo_smartcropperlib_SmartCropper_nativeCrop(JNIEnv
                                                     *env,
                                                     jclass type, jobject
                                                     srcBitmap,
                                                     jobjectArray points_, jobject
                                                     outBitmap) {
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

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_FALSE;
    }
    initClassInfo(env);
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_me_pqpo_smartcropperlib_SmartCropper_nativeInit(JNIEnv *env, jclass clazz) {
    void *handle = dlopen("libvcap_core.so", RTLD_LAZY);
    int (*createNeuralNetwork)(vcap::BuildConfig *a2);
    createNeuralNetwork = (int (*)(vcap::BuildConfig *a2)) dlsym(handle,
                                                                 "_ZN12VcapInstance19createNeuralNetworkERN4vcap15VcapBuildConfigE");
    vcap::BuildConfig config = vcap::BuildConfig();
//    config.model_file = "/data/user/0/me.pqpo.smartcropper/files/v5.vaimlite";
//    config.output_format = "NCHW";
//    config.encrypt = false;
//    config.log_level = vcap::LogLevel::INFO;
//    config.device_type = vcap::VCAPDeviceType::VCAP_ARM_CPU;
//    vcap::RuntimeConfig runtime = vcap::RuntimeConfig();
//    runtime.output_format = vcap::NCHW;
//    runtime.memory = vcap::TRAINING;
//    runtime.cpu_num_threads = 0;
//    runtime.power = vcap::LOW_PERFORMANCE;
//    runtime.precision = vcap::PRECISION_FP16;
//    runtime.framework = vcap::TENSORFLOW;
//    runtime.feature_format = vcap::NCHW;
//    runtime.dtype = vcap::DT_UINT16;
//    runtime.keep_model_memory = true;
//    runtime.quantize_type = vcap::NOQUANT;
//    runtime.priority = vcap::LOW_PRIORITY;
//    config.runtime_config = runtime;
    int re = createNeuralNetwork(&config);
    __android_log_print(ANDROID_LOG_DEBUG, "iewoo",
                        "动态加载so了..%p。。%p..%d",
                        createNeuralNetwork, handle, re);
}
void *handle;
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_wibo_doc_jni_DocDetect_initDocModel2(JNIEnv *env, jclass clazz, jbyteArray b_arr, jint i2,
                                              jboolean z, jstring str, jint i3, jbyteArray b_arr2) {
    handle = dlopen("libdoc_detect.so", RTLD_LAZY);
    bool (*doc_detect)(JNIEnv *env, jclass clazz, jbyteArray b_arr, jint i2,
                       jboolean z, jstring str, jint i3, jbyteArray b_arr2);
    doc_detect = (bool (*)(JNIEnv *env, jclass clazz, jbyteArray b_arr, jint i2,
                           jboolean z, jstring str, jint i3, jbyteArray b_arr2)) dlsym(handle,
                                                                                       "Java_com_wibo_doc_jni_DocDetect_initDocDetectModel");
    return doc_detect(env, clazz, b_arr, i2, z, str, i3, b_arr2);
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_wibo_doc_jni_DocDetect_runDetect(JNIEnv *env, jclass clazz, jobject obj,
                                          jobjectArray obj_arr, jint i2) {
    jobjectArray (*doc_detect)(JNIEnv *env, jclass clazz, jobject obj,
                       jobjectArray obj_arr, jint i2);
    doc_detect = (jobjectArray (*)(JNIEnv *env, jclass clazz, jobject obj,
                           jobjectArray obj_arr, jint i2)) dlsym(handle,
                                                                 "Java_com_wibo_doc_jni_DocDetect_runDocDetect");
    return doc_detect(env, clazz, obj,obj_arr,i2);
}