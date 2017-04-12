package com.covart.streaming_prototype_v16x16;

import com.badlogic.gdx.Gdx;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderLZ4 extends ImageDecoderBase {



    private LZ4FastDecompressor decompressor;

    ImageDecoderLZ4(){
        super();
        LZ4Factory factory = LZ4Factory.fastestInstance();
        decompressor = factory.fastDecompressor();
    }

    @Override
    public void run() {
        while(true){
            try {
                // read from network
                Buffer encodedBuf = acquireEncodedResult();
                Buffer decodeBuf = acquireImageBuffer();
                // decode: lz4 decompress
                com.covart.streaming_prototype_v16x16.Profiler.reportOnProcStart();
                decompressor.decompress(encodedBuf.data, 0, decodeBuf.data, 0, com.covart.streaming_prototype_v16x16.BufferPool.IMAGE_BUFFER_SIZE);
                com.covart.streaming_prototype_v16x16.Profiler.reportOnProcEnd();
                decodeBuf.size = com.covart.streaming_prototype_v16x16.BufferPool.IMAGE_BUFFER_SIZE;
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
