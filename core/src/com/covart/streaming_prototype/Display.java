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
        // Get display image
        // TODO: get from decoder not network!
        byte[] bufData = BufferPool.getInstance().queueNetworkToDecoder.poll();
        if(bufData != null){
            // upload to GPU!
            injectImageData(bufData);
            // release buffer
            if(!BufferPool.getInstance().queueDecoderToNetwork.offer(bufData)){
                Gdx.app.error("Display", "Cannot return the buffer to pool");
            }
        }
        if(texture != null){
            batch.draw(texture, 0, 110);
        }
    }

    public void updateEnd(){
        // record FPS
        StringPool.addField("FPS", Integer.toString(Gdx.graphics.getFramesPerSecond()));
        // draw all text

        int dy = 0;
        for(String text : StringPool.getAllText()){
            font.draw(batch, text, 0, dy);
            dy += 20;
        }

        // end batch
        batch.end();
    }

    public void injectImageData(byte[] bufData){
        // start proc
        disposeExistingTexture();
        Profiler.reportOnProcStart();
        imageBuf.rewind();
        imageBuf.put(bufData);
        imageBuf.rewind();
        texture = new Texture(image);
        // end proc
        Profiler.reportOnProcEnd();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        image.dispose();
        disposeExistingTexture();
    }

    private void disposeExistingTexture(){
        if (texture != null){
            texture.dispose();
            texture = null;
        }
    }

}
