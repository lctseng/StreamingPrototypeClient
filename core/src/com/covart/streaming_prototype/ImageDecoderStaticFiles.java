package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderStaticFiles extends ImageDecoderBase {

    private boolean loop = false;

    @Override
    public void run() {
        // open all files
        FileHandle dirHandle = Gdx.files.internal("lightfield");;
        ArrayList<FileHandle> files = new ArrayList<FileHandle>();
        Collections.addAll(files, dirHandle.list());
        int max_size = files.size();
        int count = 0;

        while(true){
            try {
                // does not care about data from network
                // non-blocking read from network
                Buffer encodedBuf = BufferPool.getInstance().queueNetworkToDecoder.poll();
                if(encodedBuf != null){
                    // blocking return to network
                    BufferPool.getInstance().queueDecoderToNetwork.put(encodedBuf);
                }
                Thread.sleep(10);
                FileHandle file = null;
                if(count >= max_size){
                    if(loop){
                        count = 0;
                        file = files.get(count);
                        count += 1;
                    }
                    // otherwise, do not load images
                }
                else{
                    file = files.get(count);
                    count += 1;
                }
                if(file != null){
                    Pixmap img = new Pixmap(file);
                    ByteBuffer pixels = img.getPixels();
                    Buffer decodeBuf = acquireImageBuffer();
                    // copy to buffer
                    decodeBuf.size = pixels.remaining();
                    pixels.get(decodeBuf.data, 0, decodeBuf.size);
                    // send back
                    sendImageResult(decodeBuf);
                }

            } catch (InterruptedException e) {
                Gdx.app.error("Decoder", "Worker interrupted");
                break;
            }
        }
    }
}
