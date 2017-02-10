package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**
 * Created by lctseng on 2017/2/6.
 */

public class Display implements Disposable{

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // buffers
    private Pixmap image;
    private ByteBuffer imageBuf;
    private Texture texture;

    private Connection conn;
    private Profiler profiler;

    // string pool

    // connection buffers
    private byte[] bufHeader;
    private byte[] bufData;

    private StreamingPrototype app;

    Display(StreamingPrototype app){
        this.app = app;

        batch = new SpriteBatch();
        font = new BitmapFont();

        image = new Pixmap(133, 200, Pixmap.Format.RGBA8888);
        imageBuf = image.getPixels();
        texture = null;

        bufHeader = new byte[4];
        bufData = new byte[106400];

        conn = null;
        profiler = Profiler.getInstance();
    }

    public void setConnection(Connection conn){
        this.conn = conn;
    }

    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // clear flash messages
        StringPool.clearFlashMessages();
    }

    public void updateEnd(){
        // draw all text
        int dy = 100;
        for(String text : StringPool.getAllText()){
            font.draw(batch, text, 0, dy);
            dy -= 20;
        }
        // end batch
        batch.end();
        // clean up
        if (texture != null){
            texture.dispose();
            texture = null;
        }
    }


    public Texture receiveNextTexture(){
        if(conn != null){
            if(conn.readn(bufHeader) == 4){
                int n = PackInteger.unpack(bufHeader);
                // read image data
                profiler.reportOnRecvStart();
                // start recv
                int r = conn.readn(bufData, n);
                if(r == n){
                    // end recv
                    profiler.reportOnRecvEnd();
                    // start proc
                    profiler.reportOnProcStart();
                    imageBuf.rewind();
                    imageBuf.put(bufData);
                    imageBuf.rewind();
                    Texture tex = new Texture(image);
                    // end proc
                    profiler.reportOnProcEnd();
                    return tex;
                }
                else{
                    Gdx.app.error("Display", "Cannot read data. Data size mismatch");
                    return null;
                }
            }
            else{
                Gdx.app.error("Display", "Cannot read header. Header size mismatch");
                return null;
            }
        }
        else{
            Gdx.app.error("Display", "No Connection object assigned");
            return null;
        }
    }

    public Texture receiveNextTexture(int imageSize){
        // read image data
        profiler.reportOnRecvStart();
        // start recv
        int r = conn.readn(bufData, imageSize);
        if(r == imageSize){
            // end recv
            profiler.reportOnRecvEnd();
            // start proc
            profiler.reportOnProcStart();
            imageBuf.rewind();
            imageBuf.put(bufData);
            imageBuf.rewind();
            Texture tex = new Texture(image);
            // end proc
            profiler.reportOnProcEnd();
            return tex;
        }
        else{
            Gdx.app.error("Display", "Cannot read data. Data size mismatch");
            return null;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        image.dispose();
    }


    public void receiveAndDisplay(int imageSize){
        texture = receiveNextTexture(imageSize);
        if(texture != null){
            batch.draw(texture, 0, 110);
        }
    }
}
