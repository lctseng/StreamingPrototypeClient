//
// Created by lctseng on 2017/2/22.
//


#include "jni.h"
#include <android/log.h>


#include "decoder.h"

static struct {
    JNIEnv *env;
    jobject instance;

} decoder_data;

static void on_frame_ready(uint8_t* frame_buf, int frame_size){
    // require a image buffer space
    // call: getDisplayBuffer
    jclass clazz = (*decoder_data.env)->GetObjectClass(decoder_data.env, decoder_data.instance);
    jmethodID getDisplayBuffer = (*decoder_data.env)->GetMethodID(decoder_data.env, clazz, "getDisplayBuffer", "()Lcom/covart/streaming_prototype/Buffer;");
    jobject displayBuffer = (*decoder_data.env)->CallObjectMethod(decoder_data.env, decoder_data.instance, getDisplayBuffer);
    if (displayBuffer != NULL) {

    }
}



JNIEXPORT jboolean JNICALL
Java_com_covart_streaming_1prototype_ImageDecoderH264_nativeDecoderInit(JNIEnv *env,
                                                                        jobject instance) {
    if(decoder_init()<0){
        return JNI_FALSE;
    }
    else {
        decoder_set_frame_ready_handler(on_frame_ready);
        return JNI_TRUE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_covart_streaming_1prototype_ImageDecoderH264_nativeDecoderCleanup(JNIEnv *env,
                                                                           jobject instance) {
    decoder_cleanup();
    return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL
Java_com_covart_streaming_1prototype_ImageDecoderH264_nativeDecoderFlush(JNIEnv *env,
                                                                         jobject instance) {

    // TODO

}

JNIEXPORT jboolean JNICALL
Java_com_covart_streaming_1prototype_ImageDecoderH264_nativeDecoderParse(JNIEnv *env,
                                                                         jobject instance,
                                                                         jobject buffer) {
    jboolean  res = JNI_FALSE;
    // set JNI env data
    decoder_data.env = env;
    decoder_data.instance = instance;
    // get the internal byte array from buffer
    jclass clazz = (*decoder_data.env)->GetObjectClass(decoder_data.env, buffer);
    jfieldID dataField = (*decoder_data.env)->GetFieldID(decoder_data.env, clazz, "data", "[B");
    jobject dataObject = (*decoder_data.env)->GetObjectField(decoder_data.env, buffer, dataField);
    jbyteArray dataArray = (jbyteArray)(dataObject);
    jbyte *data = (*decoder_data.env)->GetByteArrayElements(decoder_data.env, dataArray, NULL);
    // get the data size
    jfieldID sizeField = (*decoder_data.env)->GetFieldID(decoder_data.env, clazz, "size", "I");
    jint size = (*decoder_data.env)->GetIntField(decoder_data.env, buffer, sizeField);

    /*
    char buf[100] = {0};
    sprintf(buf, "Size to decode: %d", size);
    __android_log_write(ANDROID_LOG_INFO, "NativeH264", buf);
    */

    (*decoder_data.env)->ReleaseByteArrayElements(decoder_data.env, dataArray, data, 0);

     return res;
}

JNIEXPORT jboolean JNICALL
Java_com_covart_streaming_1prototype_ImageDecoderH264_nativeDecoder(JNIEnv *env, jobject instance,
                                                                    jbyteArray b_) {
    jbyte *b = (*env)->GetByteArrayElements(env, b_, NULL);

    // TODO

    (*env)->ReleaseByteArrayElements(env, b_, b, 0);
}