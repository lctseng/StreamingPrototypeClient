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

    public BlockingQueue<Buffer> queueNetworkToDecoder;
    public BlockingQueue<Buffer> queueDecoderToNetwork;
    public BlockingQueue<Buffer> queueDecoderToDisplay;
    public BlockingQueue<Buffer> queueDisplayToDecoder;

    private BufferPool() {
        queueNetworkToDecoder = new ArrayBlockingQueue<Buffer>(ConfigManager.getBufferQueueSize());
        queueDecoderToNetwork = new ArrayBlockingQueue<Buffer>(ConfigManager.getBufferQueueSize());
        queueDecoderToDisplay = new ArrayBlockingQueue<Buffer>(ConfigManager.getBufferQueueSize());
        queueDisplayToDecoder = new ArrayBlockingQueue<Buffer>(ConfigManager.getBufferQueueSize());
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
        for(int i=0;i<ConfigManager.getBufferQueueSize();i++){
            queueDisplayToDecoder.add(new Buffer(ConfigManager.getImageBufferSize()));
        }
    }

    private void createNetworkDecoderBuffer() {
        for(int i=0;i<ConfigManager.getBufferQueueSize();i++){
            queueDecoderToNetwork.add(new Buffer(ConfigManager.getDecoderBufferSize()));
        }
    }
}
