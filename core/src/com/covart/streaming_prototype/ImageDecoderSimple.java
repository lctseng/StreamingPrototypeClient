package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderSimple extends ImageDecoderBase {

    @Override
    public void run() {
        while(true){
            try {
                // read from network
                byte[] encodedBuf = acquireEncodedResult();
                byte[] decodeBuf = acquireImageBuffer();
                // dummy decode: copy
                Profiler.reportOnProcStart();
                System.arraycopy(encodedBuf, 0, decodeBuf, 0, Math.min(encodedBuf.length, decodeBuf.length));
                Profiler.reportOnProcEnd();
                // release buffer to network
                releaseEncodedBuffer(encodedBuf);
                // send to display
                sendImageResult(decodeBuf);

            } catch (InterruptedException e) {
                Gdx.app.error("Decoder", "Worker interrupted");
                break;
            }
        }
    }
}
