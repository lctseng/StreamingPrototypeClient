package com.covart.streaming_prototype;

import android.media.MediaCodec;

import com.badlogic.gdx.Gdx;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderH264 extends ImageDecoderBase {



    private volatile MediaCodec codec;
    private native boolean nativeDecoderInit();
    private native boolean nativeDecoderCleanup();
    private native boolean nativeDecoderParse(Buffer buffer);
    private native boolean nativeDecoderFlush();

    private boolean nativeDecoderReady;


    ImageDecoderH264(){
        super();
        System.loadLibrary("ffmpeg");
        System.loadLibrary("native-lib");
        nativeDecoderReady = nativeDecoderInit();
    }

    @Override
    public void run() {
        if(nativeDecoderReady){
            Gdx.app.log("H264", "Native decoder ready!");
        }
        else{
            Gdx.app.error("H264", "Native decoder unavailable!");
        }

        while(true){
            try {
                // read from network
                Buffer encodedBuf = acquireEncodedResult();
                // send to native decoder
                if(nativeDecoderParse(encodedBuf)){
                    Gdx.app.log("H264", "Parse OK!");
                }
                else{
                    Gdx.app.log("H264", "Parse Error!");
                }
                nativeDecoderFlush();
                // release buffer to network
                releaseEncodedBuffer(encodedBuf);
            } catch (InterruptedException e) {
                Gdx.app.error("Decoder", "Worker interrupted");
                break;
            }
        }
    }

    // call from JNI: after frame is decoded and place to Buffer
    public void onFrameReady(Buffer buf){
        try {
            sendImageResult(buf);
        } catch (InterruptedException e) {
            Gdx.app.error("H264", "Worker interrupted when onFrameReady");
        }
    }

    // call from JNI: when frame in decoded
    public Buffer getDisplayBuffer(){
        // read from network
        try {
            return acquireImageBuffer();
        } catch (InterruptedException e) {
            Gdx.app.error("H264", "Worker interrupted when getDisplayBuffer");
            return null;
        }
    }

    @Override
    protected void cleanup(){
        if(nativeDecoderReady) {
            nativeDecoderCleanup();
        }
    }
}
