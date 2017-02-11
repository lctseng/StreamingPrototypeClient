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

    // for debug
    private int count = 0;

    // connection buffers
    private byte[] bufSendingHeader;
    private byte[] bufReceivngHeader;
    private byte[] bufData;

    public Network(MasterComponentAdapter app){
        bufData = new byte[106400];
        bufSendingHeader = new byte[4];
        bufReceivngHeader = new byte[4];
        connection = new Connection(this);
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
                handleIncomingPacket();
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
    }

    @Override
    public void stop() {
        if(worker != null){
            Gdx.app.log("Network","stopping");
            worker.interrupt();
            if(Thread.currentThread() != worker){
                try {
                    worker.join();
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

    private void handleIncomingPacket(){
        // FIXME: send packet is for test only!
        count += 1;
        Message.StreamingMessage msg =
                Message.StreamingMessage.newBuilder()
                        .setType(Message.MessageType.MsgCameraInfo)
                        .setCameraMsg(
                                Message.Camera.newBuilder()
                                        .setSerialNumber(count)
                                        .build()
                        )
                        .build();
        // send!
        sendMessageProtobuf(msg);
        // FIXME: End of test packet
        // Start receiving packets
        Message.StreamingMessage pkt = readMessageProtobuf();
        if(pkt != null){
            switch(pkt.getType()){
                case MsgImage:
                    connection.readn(bufData, pkt.getImageMsg().getByteSize());
                    StringPool.addField("Image Data", String.format(Locale.TAIWAN, " %d bytes", pkt.getImageMsg().getByteSize()));
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

    private void sendMessageProtobuf(Message.StreamingMessage msg){
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
        Profiler.reportOnRecvStart();
        byte[] msg_data = new byte[bs];
        connection.readn(msg_data);
        try {
            return Message.StreamingMessage.parseFrom(msg_data);
        } catch (InvalidProtocolBufferException e) {
            Gdx.app.error("Network","Unable to receive message!");
            e.printStackTrace();
            return null;
        } finally {
            Profiler.reportOnRecvEnd();
        }
    }

    /*
    private void exchange_header(){
        // create data
        count += 1;
        Message.StreamingMessage msg =
                Message.StreamingMessage.newBuilder()
                        .setType(Message.MessageType.MsgCameraInfo)
                        .setCameraMsg(
                                Message.Camera.newBuilder()
                                        .setSerialNumber(count)
                                        .build()
                        )
                        .build();
        // send!
        byte[] sendData = msg.toByteArray();
        // write bs
        conn.write(PackInteger.pack(sendData.length));
        // write pb
        conn.write(sendData);
        // recv!
        // read bs
        byte[] bs_data = new byte[4];
        conn.read(bs_data);
        int bs = PackInteger.unpack(bs_data);
        // read pb
        Profiler.reportOnRecvStart();
        byte[] msg_data = new byte[bs];
        conn.read(msg_data);
        try {
            Message.StreamingMessage recvMsg = Message.StreamingMessage.parseFrom(msg_data);
            if(recvMsg.getType() == Message.MessageType.MsgImage){
                conn.readn(bufData, recvMsg.getImageMsg().getByteSize());
                Profiler.reportOnRecvEnd();
                display.injectImageData(bufData);
            }
            else{
                Profiler.reportOnRecvEnd();
                Gdx.app.log("Protobuf","Not an image:" + recvMsg.toString());
                conn.close();
            }
        } catch (InvalidProtocolBufferException e) {
            Profiler.reportOnRecvEnd();
            Gdx.app.error("Protobuf","Unable to receive message!!");
            e.printStackTrace();
        }
    }
    */

}
