package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StreamingPrototype extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
    BitmapFont font;
	Connection conn;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
        font = new BitmapFont();
        conn = new Connection();

        conn.connect();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        if(conn.getReady()){
            batch.draw(img, 0, 0);
        }
        else{
            // draw connection state
            font.draw(batch, "Connection is not ready!", 0, 100);
            font.draw(batch, "Touch the screen to force reconnect", 0, 80);
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
