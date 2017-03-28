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
                Buffer encodedBuf = acquireEncodedResult();
                Buffer decodeBuf = acquireImageBuffer();
                // dummy decode: copy
                Profiler.reportOnProcStart();
                System.arraycopy(encodedBuf.data, 0, decodeBuf.data, 0, Math.min(encodedBuf.data.length, decodeBuf.data.length));
                Profiler.reportOnProcEnd();
                decodeBuf.size = encodedBuf.size;
                decodeBuf.index = encodedBuf.index;
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
