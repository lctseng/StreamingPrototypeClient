package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.Buffer;
import com.covart.streaming_prototype.Utils.Profiler;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderH264 extends ImageDecoderBase {



    ;
    private native boolean nativeDecoderInit();
    private native boolean nativeDecoderCleanup();
    private native boolean nativeDecoderParse(Buffer buffer);
    private native boolean nativeDecoderFlush();

    private boolean nativeDecoderReady;
    private volatile boolean terminating;


    public ImageDecoderH264(){
        super();
        System.loadLibrary("ffmpeg");
        System.loadLibrary("native-lib");
    }

    @Override
    public void run() {
        nativeDecoderReady = nativeDecoderInit();
        if(nativeDecoderReady){
            Gdx.app.log("H264", "Native decoder ready!");
        }
        else{
            Gdx.app.error("H264", "Native decoder unavailable!");
            return;
        }
        terminating = false;

        while(!terminating){
            try {
                // read from network
                Buffer encodedBuf = acquireEncodedResult();
                Profiler.reportOnProcStart();
                // send to native decoder

                if(encodedBuf.size > 0){
                    if(!nativeDecoderParse(encodedBuf)){
                        Gdx.app.error("H264", "Parse Error!");
                    }
                }
                else{
                    nativeDecoderFlush();
                }
                if(terminating){
                    // when there is error in API, just stop it!
                    break;
                }
                if(Thread.currentThread().isInterrupted()){
                    break;
                }
                // release buffer to network
                Profiler.reportOnProcEnd();
                releaseEncodedBuffer(encodedBuf);
            } catch (InterruptedException e) {
                Gdx.app.error("Decoder", "Worker interrupted");
                break;
            }
        }
        cleanup();
    }

    // call from JNI: after frame is decoded and place to Buffer
    public void onFrameReady(Buffer buf){
        if(!terminating) {
            try {
                sendImageResult(buf);
            } catch (InterruptedException e) {
                Gdx.app.error("H264", "Worker interrupted when onFrameReady");
                terminating = true;
            }
        }
        else{
            Gdx.app.error("H264", "Worker terminating when onFrameReady");
        }
    }

    // call from JNI: when frame in decoded
    public Buffer getDisplayBuffer(){
        if(!terminating) {
            // read from network
            try {
                return acquireImageBuffer();
            } catch (InterruptedException e) {
                Gdx.app.error("H264", "Worker interrupted when getDisplayBuffer");
                terminating = true;
                return null;
            }
        }
        else{
            Gdx.app.error("H264", "Worker terminating when getDisplayBuffer");
            return null;
        }
    }

    protected void cleanup(){
        Gdx.app.log("H264", "Worker cleaning up...");
        if(nativeDecoderReady) {
            nativeDecoderCleanup();
        }
    }

    @Override
    public void stop() {
        terminating = true;
        super.stop();
    }
}
