//
// Created by lctseng on 2017/2/22.
//


#include "jni.h"
#include <android/log.h>


#include "decoder.h"


#define LOG_INFO(tag, msg) (__android_log_write(ANDROID_LOG_INFO, tag, msg))
#define LOG_ERROR(tag, msg) (__android_log_write(ANDROID_LOG_ERROR, tag, msg))

static struct {
    JNIEnv *env;
    jobject instance;

} decoder_data;


static void on_frame_ready(uint8_t* frame_buf, int frame_size){
    // require a image buffer space
    // call: getDisplayBuffer
    jclass clazz = (*decoder_data.env)->GetObjectClass(decoder_data.env, decoder_data.instance);
    jmethodID getDisplayBuffer = (*decoder_data.env)->GetMethodID(decoder_data.env, clazz, "getDisplayBuffer", "()Lcom/covart/streaming_prototype/Buffer;");
    jmethodID onFrameReady = (*decoder_data.env)->GetMethodID(decoder_data.env, clazz, "onFrameReady", "(Lcom/covart/streaming_prototype/Buffer;)V");
    jobject displayBuffer = (*decoder_data.env)->CallObjectMethod(decoder_data.env, decoder_data.instance, getDisplayBuffer);
    if (displayBuffer != NULL) {
        // write data into display buffer
        // extract the buffer space
        jclass clazz = (*decoder_data.env)->GetObjectClass(decoder_data.env, displayBuffer);
        jfieldID dataField = (*decoder_data.env)->GetFieldID(decoder_data.env, clazz, "data", "[B");
        jobject dataObject = (*decoder_data.env)->GetObjectField(decoder_data.env, displayBuffer, dataField);
        jbyteArray dataArray = (jbyteArray)(dataObject);
        jbyte *data = (*decoder_data.env)->GetByteArrayElements(decoder_data.env, dataArray, NULL);
        // memcopy
        memcpy(data, frame_buf, (size_t)frame_size);
        (*decoder_data.env)->ReleaseByteArrayElements(decoder_data.env, dataArray, data, 0);
        // set size
        jfieldID sizeField = (*decoder_data.env)->GetFieldID(decoder_data.env, clazz, "size", "I");
        (*decoder_data.env)->SetIntField(decoder_data.env, displayBuffer, sizeField, frame_size);
        // return this buffer
        (*decoder_data.env)->CallVoidMethod(decoder_data.env, decoder_data.instance, onFrameReady, displayBuffer);
    }
    else{
        LOG_ERROR("NativeH264", "Cannot get display buffer");
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

    // set JNI env data
    decoder_data.env = env;
    decoder_data.instance = instance;
    // flushing
    if(decoder_parse(NULL, 0) < 0){
        LOG_ERROR("NativeH264", "Flush Error!");
        return JNI_FALSE;
    }
    if(decoder_flush() < 0){
        LOG_ERROR("NativeH264", "Flush Error!");
        return JNI_FALSE;
    }
    return JNI_TRUE;

}

JNIEXPORT jboolean JNICALL
Java_com_covart_streaming_1prototype_ImageDecoderH264_nativeDecoderParse(JNIEnv *env,
                                                                         jobject instance,
                                                                         jobject buffer) {
    jboolean  res = JNI_TRUE;
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
    // inject into decoder
    if(decoder_parse((uint8_t*)data, size) < 0){
        LOG_ERROR("NativeH264", "Parse Error!");
        res = JNI_FALSE;
    }
    (*decoder_data.env)->ReleaseByteArrayElements(decoder_data.env, dataArray, data, 0);
     return res;
}