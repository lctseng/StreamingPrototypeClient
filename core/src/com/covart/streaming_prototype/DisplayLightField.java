package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.nio.ByteBuffer;

/**
 * Created by lctseng on 2017/2/6.
 * NTU COV-ART Lab, for NCP project
 */

public class DisplayLightField implements DisplayAdapter{

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // buffers
    private Pixmap image;
    private ByteBuffer imageBuf;
    private Texture texture;


    DisplayLightField(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        image = new Pixmap(512, 512, Pixmap.Format.RGB888);
        imageBuf = image.getPixels();
        texture = null;

        String vertexShader = Gdx.files.internal("shaders/grayscale.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/grayscale.frag").readString();
        ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        batch.setShader(shaderProgram);
    }

    @Override
    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();


        // clear flash messages
        StringPool.clearFlashMessages();
        // Get display image
        Buffer bufData = BufferPool.getInstance().queueDecoderToDisplay.poll();
        if(bufData != null){
            // upload to GPU!
            injectImageData(bufData.data);
            Profiler.reportOnDisplay();
            // release buffer
            if(!BufferPool.getInstance().queueDisplayToDecoder.offer(bufData)){
                Gdx.app.error("Display", "Cannot return the buffer to pool");
            }
        }
        if(texture != null){
            batch.draw(texture, 0, 250);
        }
    }


    @Override
    public void updateEnd(){
        // record FPS
        StringPool.addField("FPS", Integer.toString(Gdx.graphics.getFramesPerSecond()));
        // draw all text

        int dy = 20;
        for(String text : StringPool.getAllText()){
            font.draw(batch, text, 0, dy);
            dy += 20;
        }

        // end batch
        batch.end();
    }

    @Override
    public void injectImageData(byte[] bufData){
        disposeExistingTexture();
        imageBuf.rewind();
        imageBuf.put(bufData);
        imageBuf.rewind();
        texture = new Texture(image);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        image.dispose();
        disposeExistingTexture();
    }

    @Override
    public void disposeExistingTexture(){
        if (texture != null){
            texture.dispose();
            texture = null;
        }
    }

}
