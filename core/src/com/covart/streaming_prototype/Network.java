package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Locale;

import StreamingFormat.Message;

import static com.covart.streaming_prototype.Network.State.NotReady;
import static com.covart.streaming_prototype.Network.State.Ready;

/**
 * Created by lctseng on 2017/2/11.
 * NTU COV-ART Lab, for NCP project
 */

public class Network implements ConnectionListener, Runnable, Component, Disposable {

    enum State {
        NotReady, Ready
    }

    private volatile State state = NotReady;
    private Thread worker = null;
    private Connection connection;

    private MasterComponentAdapter app;

    // connection buffers
    private byte[] bufSendingHeader;
    private byte[] bufReceivngHeader;

    // sender
    private NetworkAsyncSender sender;

    private int count = 0;

    public Network(MasterComponentAdapter app){
        bufSendingHeader = new byte[4];
        bufReceivngHeader = new byte[4];
        connection = new Connection(this);
        sender = new NetworkAsyncSender(this);
        updateConnectionStateText();
        this.app = app;
    }

    public void updateConnectionStateText(){
        StringPool.addField("Connection State", connection.getStateText());
    }

    @Override
    public void onConnectionReady() {
        updateConnectionStateText();
        state = Ready;
    }

    @Override
    public void onConnectionClose() {
        updateConnectionStateText();
        state = NotReady;
        app.requireStop();
    }

    @Override
    public void onConnectionStarted(){
        updateConnectionStateText();
    }

    @Override
    public void run() {
        Gdx.app.log("Network","Worker started");
        // connect!
        connection.connect();
        // TODO: start writer
        // start receiving
        while(true){
            // check interrupt
            if(worker.isInterrupted()){
                break;
            }
            // read incoming packet
            if(state == Ready){
                try {
                    handleIncomingPacket();
                } catch (InterruptedException e) {
                    Gdx.app.log("Network","Worker interrupted");
                    e.printStackTrace();
                    app.requireStop();
                }
            }
            else{
                // Worker should not continue if network is not ready!
                break;
            }
        }
        // stop writer thread
        Gdx.app.log("Network","Worker terminated");
        connection.close();
    }

    @Override
    public void start() {
        stop();
        Gdx.app.log("Network","starting");
        worker = new Thread(this);
        worker.start();
        sender.start();
    }

    @Override
    public void stop() {
        sender.stop();
        if(worker != null){
            Gdx.app.log("Network","stopping");
            worker.interrupt();
            if(Thread.currentThread() != worker){
                try {
                    worker.join();
                    Gdx.app.log("Network","Worker stopped");
                } catch (InterruptedException e) {
                    Gdx.app.error("Network", "Cannot join worker: interrupted");
                    e.printStackTrace();
                }
            }
            worker = null;
        }
    }

    @Override
    public void dispose() {
        connection.dispose();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    private void handleIncomingPacket() throws InterruptedException {
        // Start receiving packets
        Message.StreamingMessage pkt = readMessageProtobuf();
        if(pkt != null){
            switch(pkt.getType()){
                case MsgImage:
                    // acquire new buffer
                    byte[] bufData = BufferPool.getInstance().queueDecoderToNetwork.take();
                    Profiler.reportOnRecvStart();
                    connection.readn(bufData, pkt.getImageMsg().getByteSize());
                    Profiler.reportOnRecvEnd();
                    StringPool.addField("Image Data", String.format(Locale.TAIWAN, " %d bytes", pkt.getImageMsg().getByteSize()));
                    BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                    break;
                case MsgEnding:
                    app.requireStop();
                    state = NotReady;
                    StringPool.removeField("Image Data");
                    break;
                default:
                    Gdx.app.error("Network", "Unknown packet!");
                    break;
            }
        }
    }

    public void sendMessageProtobufAsync(Message.StreamingMessage msg){
        sender.addSendMessageRequest(msg);
    }

    public void sendMessageProtobuf(Message.StreamingMessage msg){
        byte[] sendData = msg.toByteArray();
        // write bs
        PackInteger.pack(sendData.length, bufSendingHeader);
        connection.write(bufSendingHeader);
        // write pb
        connection.write(sendData);
    }

    public Message.StreamingMessage readMessageProtobuf(){
        // read bs
        if(connection.read(bufReceivngHeader) != bufReceivngHeader.length){
            Gdx.app.error("Network","Unable to receive header!");
            return null;
        }
        int bs = PackInteger.unpack(bufReceivngHeader);
        // read pb
        byte[] msg_data = new byte[bs];
        connection.readn(msg_data);
        try {
            return Message.StreamingMessage.parseFrom(msg_data);
        } catch (InvalidProtocolBufferException e) {
            Gdx.app.error("Network","Unable to receive message!");
            e.printStackTrace();
            return null;
        }
    }
}
