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

    public static final int QUEUE_SIZE = 5;
    public static final int IMAGE_BUFFER_SIZE = 106400;
    public static final int DECODER_BUFFER_SIZE = 106400;

    public BlockingQueue<byte[]> queueNetworkToDecoder;
    public BlockingQueue<byte[]> queueDecoderToNetwork;
    public BlockingQueue<byte[]> queueDecoderToDisplay;
    public BlockingQueue<byte[]> queueDisplayToDecoder;

    private BufferPool() {
        queueNetworkToDecoder = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);
        queueDecoderToNetwork = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);
        queueDecoderToDisplay = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);
        queueDisplayToDecoder = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);
        reset();
    }

    private void reset(){
        queueNetworkToDecoder.clear();
        queueDecoderToNetwork.clear();
        queueDecoderToDisplay.clear();
        queueDisplayToDecoder.clear();
        createNetworkDecoderBuffer();
        createDecoderDisplayBuffer();
    }

    private void createDecoderDisplayBuffer() {
        for(int i=0;i<QUEUE_SIZE;i++){
            queueDisplayToDecoder.add(new byte[IMAGE_BUFFER_SIZE]);
        }
    }

    private void createNetworkDecoderBuffer() {
        for(int i=0;i<QUEUE_SIZE;i++){
            queueDecoderToNetwork.add(new byte[DECODER_BUFFER_SIZE]);
        }
    }
}
