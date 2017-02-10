package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import StreamingFormat.Message;

import static com.covart.streaming_prototype.StreamingPrototype.State.Init;
import static com.covart.streaming_prototype.StreamingPrototype.State.Ready;
import static com.covart.streaming_prototype.StreamingPrototype.State.WaitForConnection;

public class StreamingPrototype extends ApplicationAdapter
        implements ConnectionListener {

    enum State {
        Init, WaitForConnection, Ready
    }

    private State state = Init;

    // debug
    private int count;

    // text drawing
    private ArrayList<String> texts;

    // major component
    private Connection conn;
    private Display display;

	
	@Override
	public void create () {
        conn = new Connection(this);
        texts = new ArrayList<String>();
        display = new Display(this);


        display.setConnection(conn);

        conn.connect();

        count = 0;

        state = WaitForConnection;

	}

    @Override
    public void onConnectionReady(){
        state = Ready;
    }

    @Override
    public void onConnectionClose(){
        state = WaitForConnection;
    }

	@Override
	public void render () {

        display.updateStart();

        if(conn.getReady()){

            float accelX = Gdx.input.getAccelerometerX();
            float accelY = Gdx.input.getAccelerometerY();
            float accelZ = Gdx.input.getAccelerometerZ();
            StringPool.addField("Sensor", String.format("Accel X = %6.4f, Y = %6.4f, , Z = %6.4f", accelX, accelY, accelZ));

            exchange_header();

            Profiler.generateProfilingStrings();
        }
        else{
            // draw connection state
            StringPool.clearFields();
            StringPool.addFlashMessage("Connection is not ready!");
            StringPool.addFlashMessage("Touch the screen to force reconnect");
            StringPool.addFlashMessage("State: " + conn.getStateText());
            // re-connect
            if(Gdx.input.isTouched()){
                // re-connect!
                conn.connect();
                Profiler.reset();
            }
        }

        display.updateEnd();
	}
	
	@Override
	public void dispose () {
        conn.dispose();
        display.dispose();
	}

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
        byte[] msg_data = new byte[bs];
        conn.read(msg_data);
        try {
            Message.StreamingMessage recvMsg = Message.StreamingMessage.parseFrom(msg_data);
            if(recvMsg.getType() == Message.MessageType.MsgImage){
                receiveAndDisplay(recvMsg.getImageMsg().getByteSize());
            }
            else{
                Gdx.app.log("Protobuf","Not an image:" + recvMsg.toString());
                conn.close();
            }
        } catch (InvalidProtocolBufferException e) {
            Gdx.app.error("Protobuf","Unable to receive message!!");
            e.printStackTrace();
        }
    }

    private void receiveAndDisplay(int imageSize){
        display.receiveAndDisplay(imageSize);
    }
}
