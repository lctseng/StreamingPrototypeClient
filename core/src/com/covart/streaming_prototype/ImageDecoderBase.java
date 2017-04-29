package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public abstract class ImageDecoderBase implements Runnable, Disposable, Component {

    private Thread worker = null;

    protected void sendImageResult(Buffer buf) throws InterruptedException {
        BufferPool.getInstance().queueDecoderToDisplay.put(buf);
    }

    protected Buffer acquireImageBuffer() throws InterruptedException {
        return BufferPool.getInstance().queueDisplayToDecoder.take();
    }

    public Buffer acquireEncodedResult() throws InterruptedException {
        return BufferPool.getInstance().queueNetworkToDecoder.take();
    }

    public void releaseEncodedBuffer(Buffer buf) throws  InterruptedException {
        BufferPool.getInstance().queueDecoderToNetwork.put(buf);
    }

    @Override
    public void start() {
        stop();
        Gdx.app.log("Decoder","starting");
        worker = new Thread(this);
        worker.start();
    }

    @Override
    public void stop() {
        if(worker != null){
            Gdx.app.log("Decoder","stopping");
            worker.interrupt();
            if(Thread.currentThread() != worker){
                try {
                    worker.join(2000);
                    Gdx.app.log("Decoder","Worker stopped");
                } catch (InterruptedException e) {
                    Gdx.app.error("Decoder", "Cannot join worker: interrupted");
                }
            }
            worker = null;
        }
    }

    @Override
    public void dispose(){
        stop();
    }

}
