package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

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

    private Component app;

    // connection buffers
    private byte[] bufData;

    public Network(Component app){
        bufData = new byte[106400];
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
        app.stop();
    }

    @Override
    public void onConnectionStarted(){
        updateConnectionStateText();
    }

    @Override
    public void run() {
        // connect!
        connection.connect();
        // TODO: start writer
        // start receiving
        while(true){
            // TODO: read pb
            // check exit thread
            if(worker.isInterrupted()){
                break;
            }
        }
        Gdx.app.log("Network","Worker terminated");
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
            // may interrupt myself! do not block here!
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
