package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import StreamingFormat.Message;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public class NetworkAsyncSender implements Component, Runnable{

    private Network network;
    private BlockingQueue<Message.StreamingMessage> messageQueue;

    private Thread worker = null;

    NetworkAsyncSender(Network network){
        this.network = network;
        messageQueue = new LinkedBlockingQueue<Message.StreamingMessage>();
    }

    @Override
    public void start() {
        stop();
        Gdx.app.log("Network Sender","starting");
        worker = new Thread(this);
        worker.start();
    }

    @Override
    public void stop() {
        if(worker != null){
            Gdx.app.log("Network Sender","stopping");
            worker.interrupt();
            if(Thread.currentThread() != worker){
                try {
                    worker.join();
                    Gdx.app.log("Network Sender","Worker stopped");
                } catch (InterruptedException e) {
                    Gdx.app.error("Network Sender", "Cannot join worker: interrupted");
                    e.printStackTrace();
                }
            }
            worker = null;
        }
    }

    @Override
    public void run() {
        while(true){
            if(Thread.currentThread().isInterrupted()){
                break;
            }
            try {
                Message.StreamingMessage msg = messageQueue.take();
                network.sendMessageProtobuf(msg);
            } catch (InterruptedException e) {
                Gdx.app.error("Network Sender", "Interrupted when wait for request");
                break;
            }
        }
    }

    public boolean addSendMessageRequest(Message.StreamingMessage msg){
        return messageQueue.offer(msg);
    }
}
