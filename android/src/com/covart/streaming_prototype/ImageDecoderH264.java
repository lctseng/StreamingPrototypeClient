package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderH264 extends ImageDecoderBase {



    private LZ4FastDecompressor decompressor;

    ImageDecoderH264(){
        super();
        LZ4Factory factory = LZ4Factory.fastestInstance();
        decompressor = factory.fastDecompressor();
    }

    @Override
    public void run() {
        while(true){
            try {
                // read from network
                byte[] encodedBuf = acquireEncodedResult();
                byte[] decodeBuf = acquireImageBuffer();
                // decode: lz4 decompress
                Profiler.reportOnProcStart();
                decompressor.decompress(encodedBuf, 0, decodeBuf, 0, BufferPool.IMAGE_BUFFER_SIZE);
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
