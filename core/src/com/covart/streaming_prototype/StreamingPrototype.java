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

public class StreamingPrototype extends ApplicationAdapter {
	private SpriteBatch batch;
    private Texture img;
    private BitmapFont font;
    private Connection conn;
    private int count;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
        font = new BitmapFont();
        conn = new Connection();

        conn.connect();
        count = 0;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(conn.getReady()){
            // write
            count += 1;
            conn.write(("Client:" + count + "\n").getBytes());
            receiveAndDisplay();
        }
        else{
            batch.begin();
            // draw connection state
            font.draw(batch, "Connection is not ready!", 0, 100);
            font.draw(batch, "Touch the screen to force reconnect", 0, 80);
            // draw state
            font.draw(batch, "State: " + conn.getStateText(), 0, 60);
            // re-connect
            if(Gdx.input.isTouched()){
                // re-connect!
                conn.connect();
            }
            batch.end();
        }

	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
        font.dispose();
        conn.dispose();
	}

    private void receiveAndDisplay(){
        // read image size

        byte[] buf_size = new byte[4];
        if(conn.readn(buf_size) == 4){
            batch.begin();
            int n = byteArrayToLeInt(buf_size);
            font.draw(batch, "Image Size:" + n, 0, 100);
            // read image data

            long start = System.nanoTime();
            long time_recv, time_proc;
            // start recv

            byte[] buf_data = new byte[n];
            int r = conn.readn(buf_data);

            // end recv
            long stop = System.nanoTime();
            time_recv = stop - start;
            start = stop;
            // start proc
            Pixmap pixmap = new Pixmap(buf_data, 0, n);
            Texture tex = new Texture(pixmap);
            batch.draw(tex, 0, 110);
            // end proc
            time_proc = System.nanoTime() - start;
            font.draw(batch, "Time for receive (ms): " + (double)time_recv * 0.001, 0, 60);
            font.draw(batch, "Time for process (ms): " + (double)time_proc * 0.001, 0, 40);
            batch.end();
            pixmap.dispose();
            tex.dispose();
        }
    }

    public static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }
}
