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


    private double total_proc = 0.0;
    private double total_recv = 0.0;
    private double n_data = 0.0;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
        font = new BitmapFont();
        conn = new Connection();
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
                total_proc = 0.0;
                total_recv = 0.0;
                n_data = 0.0;
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
            clearTextDraw();
            int n = byteArrayToLeInt(bufHeader);
            addTextDraw(String.format("Image size: %6d bytes", n));
            // read image data

            long start = System.nanoTime();
            long time_recv, time_proc;
            // start recv
            int r = conn.readn(bufData, n);
            if(r > 0){
                // end recv
                long stop = System.nanoTime();
                time_recv = stop - start;
                start = stop;
                // start proc
                Pixmap pixmap = new Pixmap(bufData, 0, n);
                ByteBuffer raw_buf = pixmap.getPixels();
                ByteBuffer finalImageBuf = image.getPixels();
                finalImageBuf.rewind();
                finalImageBuf.put(raw_buf);
                finalImageBuf.rewind();
                Texture tex = new Texture(image);
                batch.draw(tex, 0, 110);
                // end proc
                time_proc = System.nanoTime() - start;

                double f_recv = (double)time_recv * 0.000001;
                double f_proc = (double)time_proc * 0.000001;
                n_data += 1.0;
                total_proc += f_proc;
                total_recv += f_recv;

                addTextDraw(String.format("Time for receive : %6.4f ms, total: %6.4f ms, avg: %6.4f ms", f_recv, total_recv, total_recv / n_data));
                addTextDraw(String.format("Time for process : %6.4f ms, total: %6.4f ms, avg: %6.4f ms", f_proc, total_proc, total_proc / n_data));
                processTextDraw();
                batch.end();
                pixmap.dispose();
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
