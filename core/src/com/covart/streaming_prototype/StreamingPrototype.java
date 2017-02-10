package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import StreamingFormat.Message;

public class StreamingPrototype extends ApplicationAdapter {
	// basic
    private SpriteBatch batch;
    private BitmapFont font;

    // debug
    private int count;

    // text drawing
    private ArrayList<String> texts;

    // major component
    private Profiler profiler;
    private Connection conn;
    private Display display;

	
	@Override
	public void create () {
		batch = new SpriteBatch();
        font = new BitmapFont();
        conn = new Connection();
        profiler = new Profiler();
        texts = new ArrayList<String>();
        display = new Display(this);

        display.setConnection(conn);
        display.setProfiler(profiler);

        conn.connect();

        count = 0;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        clearTextDraw();

        if(conn.getReady()){

            float accelX = Gdx.input.getAccelerometerX();
            float accelY = Gdx.input.getAccelerometerY();
            float accelZ = Gdx.input.getAccelerometerZ();
            addTextDraw(String.format("Accel X = %6.4f, Y = %6.4f, , Z = %6.4f", accelX, accelY, accelZ));

            exchange_header();

            for(String s : profiler.generateProfilingStrings()){
                addTextDraw(s);
            }

        }
        else{
            // draw connection state
            addTextDraw("Connection is not ready!");
            addTextDraw("Touch the screen to force reconnect");
            addTextDraw("State: " + conn.getStateText());
            // re-connect
            if(Gdx.input.isTouched()){
                // re-connect!
                conn.connect();
                profiler.reset();
            }
        }
        batch.begin();
        processTextDraw();
        batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
        font.dispose();
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
        Texture tex = display.receiveNextTexture(imageSize);
        if(tex != null){
            batch.begin();
            batch.draw(tex, 0, 110);
            batch.end();
            tex.dispose();
        }
    }

    private void clearTextDraw(){
        texts.clear();
    }

    public void addTextDraw(String text){
        texts.add(text);
    }

    private void processTextDraw(){
        int dy = 100;
        for(String text : texts){
            font.draw(batch, text, 0, dy);
            dy -= 20;
        }
    }
}
