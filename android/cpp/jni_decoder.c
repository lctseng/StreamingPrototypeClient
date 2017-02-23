//
// Created by lctseng on 2017/2/22.
//


#include "jni.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"



JNIEXPORT jstring JNICALL
Java_com_covart_streaming_1prototype_StreamingPrototype_stringFromJNI(JNIEnv *env,
                                                                      jobject instance) {

    // compiling
    //

    avcodec_register_all();
    struct AVPacket packet;

    return (*env)->NewStringUTF(env, "Hello from JNI !");
}