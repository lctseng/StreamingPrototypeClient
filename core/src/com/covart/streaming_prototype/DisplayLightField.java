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


    private ShaderProgram shaderProgram;
    private int lf_counter;

    final static int GRID_WIDTH = 8;
    final static int TOTAL_IMAGES = GRID_WIDTH * GRID_WIDTH;
    final static int DIMENSION = 512;

    final static boolean SHOW_SOURCE = true;

    DisplayLightField(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        image = new Pixmap(DIMENSION * GRID_WIDTH, DIMENSION * GRID_WIDTH, Pixmap.Format.RGB888);
        imageBuf = image.getPixels();
        texture = null;

        String vertexShader = Gdx.files.internal("shaders/grayscale.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/grayscale.frag").readString();
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);

        lf_counter = 0;
    }

    @Override
    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // collect images
        if(lf_counter < TOTAL_IMAGES){
            Buffer src = BufferPool.getInstance().queueDecoderToDisplay.poll();
            if(src != null){
                // copy images from buffer
                int row = lf_counter / GRID_WIDTH;
                int col = lf_counter % GRID_WIDTH;
                // store one row at a time
                for(int row_idx=0;row_idx < DIMENSION;row_idx++){
                    int global_row_offset = row *  GRID_WIDTH * DIMENSION * DIMENSION * 3;
                    int global_col_offset = col * DIMENSION * 3;
                    int local_row_offset = row_idx *  GRID_WIDTH * DIMENSION * 3;
                    imageBuf.position(global_row_offset + local_row_offset + global_col_offset);
                    imageBuf.put(src.data, row_idx * DIMENSION * 3, DIMENSION * 3);
                }
                //imageBuf.put(src.data,0,BufferPool.IMAGE_BUFFER_SIZE);
                // end of copy
                Gdx.app.log("LightField Display", "Loading light field:" + ++lf_counter);
                if(!BufferPool.getInstance().queueDisplayToDecoder.offer(src)){
                    Gdx.app.error("LightField Display", "Cannot return the buffer to pool");
                }
                if(lf_counter == TOTAL_IMAGES){
                    // done, create texture
                    imageBuf.rewind();
                    texture = new Texture(image);
                    Gdx.app.log("LightField Display", "Tile image created");
                }
            }
        }

        batch.begin();
        // clear flash messages
        StringPool.clearFlashMessages();
        if(SHOW_SOURCE) {
            if (texture != null) {
                batch.draw(texture, 0, 250, 700, 700);
            }
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
        lf_counter = 0;
    }

}
