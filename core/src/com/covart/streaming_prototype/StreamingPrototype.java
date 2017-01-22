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
            byte[] text = new byte[100];
            conn.read(text);
            batch.draw(img, 0, 0);
            font.draw(batch, new String(text), 0, 100);
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
