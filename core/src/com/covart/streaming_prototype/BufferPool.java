package com.covart.streaming_prototype;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */
public class BufferPool {
    private static BufferPool ourInstance = new BufferPool();

    public static BufferPool getInstance() {
        return ourInstance;
    }

    public static final int QUEUE_SIZE = 3;
    public static final int IMAGE_BUFFER_SIZE = DisplayLightField.DIMENSION * DisplayLightField.DIMENSION * 3;
    public static final int DECODER_BUFFER_SIZE = IMAGE_BUFFER_SIZE;

    public BlockingQueue<Buffer> queueNetworkToDecoder;
    public BlockingQueue<Buffer> queueDecoderToNetwork;
    public BlockingQueue<Buffer> queueDecoderToDisplay;
    public BlockingQueue<Buffer> queueDisplayToDecoder;

    private BufferPool() {
        queueNetworkToDecoder = new ArrayBlockingQueue<Buffer>(QUEUE_SIZE);
        queueDecoderToNetwork = new ArrayBlockingQueue<Buffer>(QUEUE_SIZE);
        queueDecoderToDisplay = new ArrayBlockingQueue<Buffer>(QUEUE_SIZE);
        queueDisplayToDecoder = new ArrayBlockingQueue<Buffer>(QUEUE_SIZE);
        reset();
    }

    public void reset(){
        queueNetworkToDecoder.clear();
        queueDecoderToNetwork.clear();
        queueDecoderToDisplay.clear();
        queueDisplayToDecoder.clear();
        createNetworkDecoderBuffer();
        createDecoderDisplayBuffer();
    }

    private void createDecoderDisplayBuffer() {
        for(int i=0;i<QUEUE_SIZE;i++){
            queueDisplayToDecoder.add(new Buffer(IMAGE_BUFFER_SIZE));
        }
    }

    private void createNetworkDecoderBuffer() {
        for(int i=0;i<QUEUE_SIZE;i++){
            queueDecoderToNetwork.add(new Buffer(DECODER_BUFFER_SIZE));
        }
    }
}
