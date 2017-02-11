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
 * NTU COV-ART Lab, for NCP project
 */

public class Display implements Disposable{

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // buffers
    private Pixmap image;
    private ByteBuffer imageBuf;
    private Texture texture;
    // string pool

    Display(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        image = new Pixmap(133, 200, Pixmap.Format.RGBA8888);
        imageBuf = image.getPixels();
        texture = null;

    }

    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // clear flash messages
        StringPool.clearFlashMessages();
    }

    public void updateEnd(){
        // record FPS
        StringPool.addField("FPS", Integer.toString(Gdx.graphics.getFramesPerSecond()));
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

    public void injectImageData(byte[] bufData){
        // start proc
        Profiler.reportOnProcStart();
        imageBuf.rewind();
        imageBuf.put(bufData);
        imageBuf.rewind();
        texture = new Texture(image);
        // end proc
        Profiler.reportOnProcEnd();
        batch.draw(texture, 0, 110);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        image.dispose();
    }

}
