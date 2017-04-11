package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class ImageDecoderStaticFiles extends ImageDecoderBase {

    private boolean loop = false;
    public final boolean ROW_MAJOR = false;

    @Override
    public void run() {
        // open all files
        FileHandle dirHandle = Gdx.files.internal("lightfield");;
        ArrayList<FileHandle> files = new ArrayList<FileHandle>();
        Collections.addAll(files, dirHandle.list());
        Collections.sort(files, new Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle file1, FileHandle file2)
            {
                if(ROW_MAJOR){
                    return file1.nameWithoutExtension().compareTo(file2.nameWithoutExtension());
                }
                else{
                    // column major
                    Scanner s1 = new Scanner(file1.nameWithoutExtension()).useDelimiter("[^0-9]+");
                    Scanner s2 = new Scanner(file2.nameWithoutExtension()).useDelimiter("[^0-9]+");
                    int row1 = s1.nextInt();
                    int col1 = s1.nextInt();
                    int row2 = s2.nextInt();
                    int col2 = s2.nextInt();
                    Integer val1 = col1 * 100 + row1;
                    Integer val2 = col2 * 100 + row2;
                    return  val1.compareTo(val2);
                }
            }
        });
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
                //Thread.sleep(200);
                FileHandle file = null;
                int index = count / DisplayLightField.ROW_WIDTH;
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
                    decodeBuf.index = index;
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
