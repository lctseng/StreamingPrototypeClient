package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
		batch.begin();
        if(conn.getReady()){
            // write
            count += 1;
            conn.write(("Client:" + count + "\n").getBytes());
            // read
            long start = System.nanoTime();
            long time_recv, time_proc;
            byte[] text = new byte[100];
            conn.read(text);
            long stop = System.nanoTime();
            time_recv = stop - start;
            start = stop;
            batch.draw(img, 0, 110);
            time_proc = System.nanoTime() - start;
            font.draw(batch, new String(text), 0, 100);
            font.draw(batch, "Time for receive (ms): " + (double)time_recv * 0.001, 0, 80);
            font.draw(batch, "Time for process (ms): " + (double)time_proc * 0.001, 0, 60);
        }
        else{
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
        }
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
        font.dispose();
        conn.dispose();
	}
}
