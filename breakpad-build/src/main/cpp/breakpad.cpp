#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <pthread.h>

#include "client/linux/handler/exception_handler.h"
#include "client/linux/handler/minidump_descriptor.h"

#define LOG_TAG "dodoodla_crash"

#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

//全局变量
JavaVM *g_jvm = NULL;
jclass BreakpadInit = NULL;
jmethodID uploadDumpFile = NULL;


    void upload(jstring s) {

        JNIEnv *env;
        void *args;

        //Attach主线程
        if(g_jvm->AttachCurrentThread(&env,args)!= JNI_OK){
            ALOGD("%s: AttachCurrentThread() failed", __FUNCTION__);
            return ;
        }

        if (BreakpadInit == 0) {
            ALOGD("找不到类名");
            return;
        }

        if (uploadDumpFile == 0) {
            ALOGD("找不到方法名");
            return;
        }
        ALOGD("上报中......");
        env->CallStaticVoidMethod(BreakpadInit, uploadDumpFile, s);
    }

    void* runMethod(void *args) {

        JNIEnv *env;
        jclass cls;

        //得到线程创建的参数
        int* num = (int*)args;
        //Attach主线程
        if(g_jvm->AttachCurrentThread(&env,args)!= JNI_OK){
            ALOGD("Attach主线程失败");
            ALOGD("%s: AttachCurrentThread() failed", __FUNCTION__);
            return NULL;
        } else{
            ALOGD("Attach主线程");
        }

        //找到对应的类
        cls = env->FindClass("com/sample/breakpad/BreakpadInit");
        if(cls == NULL){
            ALOGD("FindClass() Error.....");
            return NULL;
        }else{
            ALOGD("找到对应的类");
        }

        //找到对应的方法名
        jmethodID toastID = env->GetStaticMethodID(BreakpadInit, "uploadDumpFile", "(Ljava/lang/String;)V");
        if (toastID == 0) {
            ALOGD("GetStaticMethodID() Error.....");
            return NULL;
        }else{
            ALOGD("找到对应的方法名");
        }
        env->CallStaticVoidMethod(BreakpadInit, uploadDumpFile, env->NewStringUTF("我擦。。。。。。。"));


        //Detach主线程
        if(g_jvm->DetachCurrentThread() != JNI_OK){
            ALOGD("%s: DetachCurrentThread() failed", __FUNCTION__);
        }

        //线程自杀，需要返回参数
        pthread_exit((void*)200);

    }


    //自定义 dump 完成回调
    bool DumpCallback(const google_breakpad::MinidumpDescriptor &descriptor,
                      void *context,
                      bool succeeded) {
        ALOGD("===============捕获crash成功================");
        return succeeded;
    }

    //初始化 Breakpad，java 层调用的入口
    extern "C"
    JNIEXPORT void JNICALL Java_com_sample_breakpad_BreakpadInit_initBreakpadNative(JNIEnv *env, jclass type, jstring path_) {

        //保存全局JVM以便在子线程中使用
        env->GetJavaVM(&g_jvm);
        //保存全局的 jclass
        BreakpadInit = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("com/sample/breakpad/BreakpadInit")));
        uploadDumpFile = env->GetStaticMethodID(BreakpadInit, "uploadDumpFile", "(Ljava/lang/String;)V");

        const char *path = env->GetStringUTFChars(path_, 0);
        //初始化 MinidumpDescriptor
        google_breakpad::MinidumpDescriptor descriptor(path);
        //初始化 ExceptionHandler，注册 dump 完毕的回调
        static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);
        ALOGD("初始化成功，dump 文件存放地址是：%s", path);
        env->ReleaseStringUTFChars(path_, path);

//        upload(env->NewStringUTF("第一个成功了吗？"));

//        int l = 100;
//        pthread_t pt;
//        //创建子线程
//        pthread_create(&pt, NULL, runMethod, (void*)l);
//        void* r_val;
//        //等待线程结束，获取线程返回参数
//        pthread_join(pt, &r_val);
//        printf("return value : %d", (int)r_val);
    }


    //JNI_OnLoad方法是在动态库被加载时调用
    //JavaVM 是虚拟机在 JNI 中的表示，一个 JVM 中只有一个 JavaVM 对象，这个对象是线程共享的。
    JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
            //JNIEnv 类型是一个指向全部 JNI 方法的指针
            JNIEnv *env;
            if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
                return JNI_ERR;
            }
            return JNI_VERSION_1_6;
    }