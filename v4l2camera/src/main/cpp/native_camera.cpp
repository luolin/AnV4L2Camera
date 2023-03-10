//
// Created by llm on 20-7-8.
//
#include <jni.h>
#include <android/log.h>
#include <assert.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "V4L2Camera.h"

//定义日志打印宏函数
#define LOG_TAG "FFMediaPlayer"
#define ALOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGW(...)  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//#define VIDEO_FILE "/dev/video0"
#define CAMERA_COUNT 8
const char* VIDEO_FILE[] ={"/dev/video0", "/dev/video1", "/dev/video2", "/dev/video3", "/dev/video8", "/dev/video9", "/dev/video10", "/dev/video11"};
#define IMAGEWIDTH      1280
#define IMAGEHEIGHT     720
#define PIX_FORMATE     V4L2_PIX_FMT_NV12

V4L2Camera* v4l2Camera[CAMERA_COUNT];
JavaVM *javaVM = 0;

static void com_iview_camera_native_init(JNIEnv *env,jobject thiz) {
    for(int i=0; i<CAMERA_COUNT; i++) {
        if(v4l2Camera[i] == 0) {
            v4l2Camera[i] = new V4L2Camera();
            //由v4l2负责释放
            JavaCallHelper *javaCallHelper = new JavaCallHelper(javaVM, env, thiz, i);
            v4l2Camera[i]->setListener(javaCallHelper);
        }
    }
}

static void com_iview_camera_native_release(JNIEnv *env) {;
    for(int i=0; i<CAMERA_COUNT; i++) {
        if (v4l2Camera[i] != 0) {
            v4l2Camera[i]->setListener(0);
            delete v4l2Camera[i];
            v4l2Camera[i] = 0;
        }
    }
}

static jint com_iview_camera_native_open(JNIEnv *env, jobject thiz, jint cameraid) {

    int ret = ERROR_OPEN_FAIL;
    if (v4l2Camera[cameraid] != 0) {
        ret = v4l2Camera[cameraid]->Open(VIDEO_FILE[cameraid], IMAGEWIDTH, IMAGEHEIGHT, PIX_FORMATE);
    }

    return ret;
}

static void com_iview_camera_native_close(JNIEnv *env, jobject thiz, jint cameraid) {
    if (v4l2Camera != 0) {
        v4l2Camera[cameraid]->Close();
    }
}

static jobject com_iview_camera_native_getParameters(JNIEnv *env, jobject thiz, jint cameraid) {

    if (v4l2Camera[cameraid] == 0) {
        return 0;
    }
    std::list<Parameter> parameters = v4l2Camera[cameraid]->getParameters();

    jclass list_class = env->FindClass("java/util/ArrayList");
    if (list_class == NULL) {
        return 0;
    }

    jmethodID list_costruct = env->GetMethodID(list_class , "<init>","()V"); //获得得构造函数Id
    jmethodID list_add = env->GetMethodID(list_class, "add", "(Ljava/lang/Object;)Z");
    jobject list_obj = env->NewObject(list_class , list_costruct); //创建一个Arraylist集合对象

    jclass parameter_cls = env->FindClass("pri/tool/bean/Parameter");//获得类引用
    //获得该类型的构造函数  函数名为 <init> 返回类型必须为 void 即 V
    jmethodID parameter_costruct = env->GetMethodID(parameter_cls , "<init>", "(ILjava/util/ArrayList;)V");

    jclass frame_cls = env->FindClass("pri/tool/bean/Frame");//获得类引用
    //获得该类型的构造函数  函数名为 <init> 返回类型必须为 void 即 V
    jmethodID frame_costruct = env->GetMethodID(frame_cls , "<init>", "(IILjava/util/ArrayList;)V");

    jclass frameRate_cls = env->FindClass("pri/tool/bean/FrameRate");//获得类引用
    //获得该类型的构造函数  函数名为 <init> 返回类型必须为 void 即 V
    jmethodID frameRate_costruct = env->GetMethodID(frameRate_cls , "<init>", "(II)V");

    for (Parameter parameter : parameters) {

        jobject listFrame_obj = env->NewObject(list_class , list_costruct); //创建一个Arraylist集合对象
        for (Frame frame : parameter.frames) {

            jobject listFrameRate_obj = env->NewObject(list_class , list_costruct); //创建一个Arraylist集合对象
            for (FrameRate frameRate : frame.frameRate) {
                jobject frameRate_obj = env->NewObject(frameRate_cls, frameRate_costruct, frameRate.numerator, frameRate.denominator);
                env->CallBooleanMethod(listFrameRate_obj , list_add , frameRate_obj);
            }

            jobject frame_obj = env->NewObject(frame_cls, frame_costruct, frame.width, frame.height, listFrameRate_obj);
            env->CallBooleanMethod(listFrame_obj , list_add , frame_obj);
        }

        //TODO:完善类型映射
        int format;
        switch(parameter.pixFormat) {
            case V4L2_PIX_FMT_YUYV:
                format = YUYV;
                break;
            case V4L2_PIX_FMT_MJPEG:
                format = MJPEG;
                break;
            case V4L2_PIX_FMT_H264:
                format = H264;
                break;
            case V4L2_PIX_FMT_NV12:
                format = NV12;
                break;
            default:
                format = parameter.pixFormat;
                break;
        }
        jobject parameter_obj = env->NewObject(parameter_cls, parameter_costruct, format, listFrame_obj);
        env->CallBooleanMethod(list_obj, list_add, parameter_obj);
    }

    return list_obj;
}

static jint com_iview_camera_native_setPreviewSize(JNIEnv *env, jobject thiz, jint cameraid, jint width, jint height, jint pixformat) {
    if (v4l2Camera[cameraid] == 0) {
        return ERROR_CAPABILITY_UNSUPPORT;
    }

    int format;
    switch (pixformat) {
        case NV12:
            format = V4L2_PIX_FMT_NV12;
            break;
        case YUYV:
            format = V4L2_PIX_FMT_YUYV;
            break;
        case MJPEG:
            format = V4L2_PIX_FMT_MJPEG;
            break;
        default:
            format = -1;
            break;
    }

    if (format == -1) {
        return ERROR_CAPABILITY_UNSUPPORT;
    }

    return v4l2Camera[cameraid]->setPreviewSize(width, height, format);
}

static int com_iview_camera_native_setSurface(JNIEnv *env, jobject thiz, jint cameraid, jobject surface) {
    if (v4l2Camera == 0) {
        return ERROR_CAPABILITY_UNSUPPORT;
    }

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);

    v4l2Camera[cameraid]->setSurface(window);

    return 0;
}

static int com_iview_camera_native_startPreview(JNIEnv *env, jobject thiz, jint cameraid) {
    if (v4l2Camera == 0) {
        return ERROR_CAPABILITY_UNSUPPORT;
    }
    int ret = 0;
    ret = v4l2Camera[cameraid]->Init();
    if (ret != 0 ) {
        ALOGE("startPreview init fail");
    }
    v4l2Camera[cameraid]->StartStreaming();

    return 0;
}

static int com_iview_camera_native_stopPreview(JNIEnv *env, jobject thiz, jint cameraid) {
    if (v4l2Camera == 0) {
        return ERROR_CAPABILITY_UNSUPPORT;
    }

    v4l2Camera[cameraid]->StopStreaming();

    return 0;
}


static JNINativeMethod gMethods[] = {
{"native_init",         "()V",                              (void *)com_iview_camera_native_init},
{"native_release",         "()V",                              (void *)com_iview_camera_native_release},
{"native_open",         "(I)I",                              (void *)com_iview_camera_native_open},
{"native_close",         "(I)V",                              (void *)com_iview_camera_native_close},
{"native_getParameters",         "(I)Ljava/util/ArrayList;",                              (void *)com_iview_camera_native_getParameters},
{"native_setPreviewSize",         "(IIII)I",                              (void *)com_iview_camera_native_setPreviewSize},
{"native_setSurface",         "(ILjava/lang/Object;)I",                              (void *)com_iview_camera_native_setSurface},
{"native_startPreview",         "(I)I",                              (void *)com_iview_camera_native_startPreview},
{"native_stopPreview",         "(I)I",                              (void *)com_iview_camera_native_stopPreview},
};

//Ljava/lang/Object;
//Ljava/util/ArrayList;
static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int methodsNum) {
    jclass clazz;
    //找到声明native方法的类
    clazz = env->FindClass(className);
    if(clazz == NULL){
        return JNI_FALSE;
    }
    //注册函数 参数：java类 所要注册的函数数组 注册函数的个数
    if(env->RegisterNatives(clazz, gMethods, methodsNum) < 0){
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static int register_native_methods(JNIEnv* env){
    //指定类的路径，通过FindClass 方法来找到对应的类
    const char* className  = "pri/tool/v4l2camera/V4L2Camera";

    return registerNativeMethods(env, className, gMethods, sizeof(gMethods)/ sizeof(gMethods[0]));
}

jint JNI_OnLoad(JavaVM* vm, void* /* reserved */)
{
    JNIEnv* env = NULL;
    jint result = -1;

    javaVM = vm;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (register_native_methods(env) < 0) {
        ALOGE("ERROR: register_native_methods failed");
        goto bail;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    bail:
    return result;
}
