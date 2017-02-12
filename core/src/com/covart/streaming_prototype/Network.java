package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.google.protobuf.InvalidProtocolBufferException;

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
        Gdx.app.log("Network","Connection Ready");
        updateConnectionStateText();
        state = Ready;
        sendInitPacket();
    }

    @Override
    public void onConnectionClose() {
        Gdx.app.log("Network","Connection Closed");
        updateConnectionStateText();
        state = NotReady;
        this.app.requireStop();
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
        // send init packet
        // start receiving
        while(true){
            // check interrupt
            if(worker.isInterrupted()){
                Gdx.app.log("Network","Worker interrupted");
                break;
            }
            // read incoming packet
            if(state == Ready){
                try {
                    if(!handleIncomingPacket()){
                        Gdx.app.log("Network","Worker terminated due to unable to receive header");
                        break;
                    }
                } catch (InterruptedException e) {
                    Gdx.app.log("Network","Worker interrupted by exception");
                    break;
                }
            }
            else{
                Gdx.app.log("Network","Worker terminate due to not ready!");
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
            Gdx.app.log("Network","Closing connection");
            connection.close();
            Gdx.app.log("Network","stopping worker");
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

    public Connection getConnection() {
        return connection;
    }

    private boolean handleIncomingPacket() throws InterruptedException {
        // Start receiving packets
        Message.StreamingMessage pkt = readMessageProtobuf();
        if(pkt != null){
            this.app.dispatchMessage(pkt);
            return true;
        }
        else{
            return false;
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
        if(connection.readn(bufReceivngHeader) != bufReceivngHeader.length){
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

    public void sendInitPacket(){
        // crafting packet
        Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                .setType(Message.MessageType.MsgInit)
                .setInitMsg(
                        Message.Init.newBuilder()
                                .setModuleID(1)
                                .setWidth(Gdx.graphics.getWidth())
                                .setHeight(Gdx.graphics.getHeight())
                                .build()

                ).build();
        sendMessageProtobufAsync(msg);
    }
}
