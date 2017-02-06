package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class StreamingPrototype extends ApplicationAdapter {
	private SpriteBatch batch;
    private BitmapFont font;
    private Connection conn;
    private int count;
    private Pixmap image;

    // connection buffers
    private byte[] bufHeader;
    private byte[] bufData;

    private ArrayList<String> texts;

    // profiler
    private Profiler profiler;

	
	@Override
	public void create () {
		batch = new SpriteBatch();
        font = new BitmapFont();
        conn = new Connection();
        profiler = new Profiler();
        texts = new ArrayList<String>();
        image = new Pixmap(133, 200, Pixmap.Format.RGBA8888);

        bufHeader = new byte[4];
        bufData = new byte[106400];

        conn.connect();
        count = 0;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(conn.getReady()){
            clearTextDraw();
            float accelX = Gdx.input.getAccelerometerX();
            float accelY = Gdx.input.getAccelerometerY();
            float accelZ = Gdx.input.getAccelerometerZ();
            addTextDraw(String.format("Accel X = %6.4f, Y = %6.4f, , Z = %6.4f", accelX, accelY, accelZ));
            // write
            count += 1;
            conn.write(("Client:" + count + "\n").getBytes());
            receiveAndDisplay();
        }
        else{
            batch.begin();
            clearTextDraw();
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
            processTextDraw();
            batch.end();
        }

	}
	
	@Override
	public void dispose () {
		batch.dispose();
        font.dispose();
        conn.dispose();
        image.dispose();
	}

    private void receiveAndDisplay(){
        // read image size

        if(conn.readn(bufHeader) == 4){
            batch.begin();

            int n = byteArrayToLeInt(bufHeader);
            addTextDraw(String.format("Image size: %6d bytes", n));
            // read image data

            profiler.reportOnRecvStart();
            // start recv
            int r = conn.readn(bufData, n);
            if(r > 0){
                // end recv
                profiler.reportOnRecvEnd();
                // start proc
                profiler.reportOnProcStart();
                ByteBuffer finalImageBuf = image.getPixels();
                finalImageBuf.rewind();
                finalImageBuf.put(bufData);
                finalImageBuf.rewind();
                Texture tex = new Texture(image);
                batch.draw(tex, 0, 110);
                // end proc
                profiler.reportOnProcEnd();
                // add profiler text
                for(String s : profiler.generateProfilingStrings()){
                    addTextDraw(s);
                }
                processTextDraw();
                batch.end();
                tex.dispose();
            }
            else {
                processTextDraw();
                batch.end();
            }
        }
    }

    public static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    private void clearTextDraw(){
        texts.clear();
    }

    private void addTextDraw(String text){
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
