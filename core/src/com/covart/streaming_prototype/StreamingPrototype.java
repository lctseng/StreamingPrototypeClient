package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

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
            // write
            count += 1;
            conn.write(("Client:" + count + "\n").getBytes());


            receiveAndDisplay();

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

    private void receiveAndDisplay(){
        Texture tex = display.receiveNextTexture();
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
