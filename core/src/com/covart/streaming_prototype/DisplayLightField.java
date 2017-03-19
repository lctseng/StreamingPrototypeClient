package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import java.nio.ByteBuffer;

/**
 * Created by lctseng on 2017/2/6.
 * NTU COV-ART Lab, for NCP project
 */

public class DisplayLightField extends DisplayBase{


    final static int COL_WIDTH = 16;
    final static int ROW_WIDTH = 8;
    final static int TOTAL_IMAGES = COL_WIDTH * ROW_WIDTH;
    final static int DIMENSION = 512;

    final static boolean SHOW_SOURCE = false;

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // multi-texture
    private Texture[] texture_slots;
    private Pixmap slotImage;
    private ByteBuffer slotImageBuf;


    private ShaderProgram shaderProgram;
    private int lf_counter;

    private Mesh mesh;

    private boolean lf_ready;

    private float focus = 0.0f;
    private float aperture = 5.0f;
    private float cameraPositionX =0.5f;
    private float cameraPositionY =0.5f;


    private Texture tex_control;


    DisplayLightField(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        tex_control = new Texture("badlogic.jpg");

        // multi-texture
        texture_slots = new Texture[COL_WIDTH];
        slotImage = new Pixmap(DIMENSION, DIMENSION * ROW_WIDTH, Pixmap.Format.RGB888);
        slotImageBuf = slotImage.getPixels();

        String vertexShader = Gdx.files.internal("shaders/lightfield.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/lightfield.frag").readString();
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        Gdx.app.error("GLSL", "Compiling:" + shaderProgram.isCompiled());
        Gdx.app.error("GLSL", shaderProgram.getLog());
        for(String str : shaderProgram.getUniforms()){
            Gdx.app.error("GLSL", "Uniform:" + str);
        }
        ShaderProgram.pedantic = false;
        shaderProgram.setUniformf("focusPoint", focus);
        shaderProgram.setUniformf("apertureSize", aperture);
        shaderProgram.setUniformi("rows", ROW_WIDTH);
        shaderProgram.setUniformi("cols", COL_WIDTH);



        lf_counter = 0;
        lf_ready = false;

        float[] verts = new float[36];
        int i = 0;
        float x,y; // Mesh location in the world
        float width,height; // Mesh width and height

        x = y = -1.0f;
        width = height = 2f;

        //Top Left Vertex Triangle 1
        verts[i++] = x;   //X
        verts[i++] = y + height; //Y
        verts[i++] = 0;    //Z
        verts[i++] = 1.0f; // W
        verts[i++] = 0f;   //U
        verts[i++] = 0f;   //V

        //Top Right Vertex Triangle 1
        verts[i++] = x + width;
        verts[i++] = y + height;
        verts[i++] = 0;
        verts[i++] = 1.0f;
        verts[i++] = 1f;
        verts[i++] = 0f;

        //Bottom Left Vertex Triangle 1
        verts[i++] = x;
        verts[i++] = y;
        verts[i++] = 0;
        verts[i++] = 1.0f;
        verts[i++] = 0f;
        verts[i++] = 1f;

        //Top Right Vertex Triangle 2
        verts[i++] = x + width;
        verts[i++] = y + height;
        verts[i++] = 0;
        verts[i++] = 1.0f;
        verts[i++] = 1f;
        verts[i++] = 0f;

        //Bottom Right Vertex Triangle 2
        verts[i++] = x + width;
        verts[i++] = y;
        verts[i++] = 0;
        verts[i++] = 1.0f;
        verts[i++] = 1f;
        verts[i++] = 1f;

        //Bottom Left Vertex Triangle 2
        verts[i++] = x;
        verts[i++] = y;
        verts[i++] = 0;
        verts[i++] = 1.0f;
        verts[i++] = 0f;
        verts[i] = 1f;

        // Create a mesh out of two triangles rendered clockwise without indices
        mesh = new Mesh( true, 6, 0,
                new VertexAttribute( VertexAttributes.Usage.Position, 4, ShaderProgram.POSITION_ATTRIBUTE ),
                new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ) );

        mesh.setVertices(verts);

    }

    @Override
    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // collect images
        if(!lf_ready && lf_counter < TOTAL_IMAGES){
            Buffer src = BufferPool.getInstance().queueDecoderToDisplay.poll();
            if(src != null){
                // copy images from buffer
                int col = lf_counter / ROW_WIDTH;
                int row = lf_counter % ROW_WIDTH;
                // just concat all images
                slotImageBuf.put(src.data, 0, src.size);

                Gdx.app.log("LightField Display", "Loading light field:" + ++lf_counter);
                if(!BufferPool.getInstance().queueDisplayToDecoder.offer(src)){
                    Gdx.app.error("LightField Display", "Cannot return the buffer to pool");
                }

                // if last row, rewind the buffer and submit the texture
                if(row == 7){
                    slotImageBuf.rewind();
                    texture_slots[col] = new Texture(slotImage);
                    slotImageBuf.rewind();
                }
                if(lf_counter == TOTAL_IMAGES){
                    // done
                    Gdx.app.log("LightField Display", "Tile image created");
                    lf_ready = true;
                }
            }
        }




        if(lf_ready && !SHOW_SOURCE){
            shaderProgram.begin();
            // interpolate LF
            for(int i=0;i<COL_WIDTH;i++){
                texture_slots[i].bind(i);
                shaderProgram.setUniformi("u_custom_texture" + i, i);
            }
            Matrix4 modelviewMatrix = new Matrix4();
            Matrix4 projectionMatrix = new Matrix4();
            projectionMatrix.setToOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
            // set matrix
            shaderProgram.setUniformMatrix("projectionMatrix", projectionMatrix);
            shaderProgram.setUniformMatrix("modelviewMatrix", modelviewMatrix);
            // set camera params
            shaderProgram.setUniformi("rows", ROW_WIDTH);
            shaderProgram.setUniformi("cols", COL_WIDTH);
            shaderProgram.setUniformf("focusPoint", focus);
            shaderProgram.setUniformf("apertureSize", aperture);
            shaderProgram.setUniformf("cameraPositionX", cameraPositionX);
            shaderProgram.setUniformf("cameraPositionY", cameraPositionY);
            Gdx.app.log("LightField Display", "X: " + cameraPositionX + " , Y: " + cameraPositionY);
            // draw!
            mesh.render(shaderProgram, GL20.GL_TRIANGLES);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            shaderProgram.end();

        }



        batch.begin();
        // draw control
        batch.draw(tex_control, 0, Gdx.graphics.getHeight() - 150, 150, 150);
        // clear flash messages
        StringPool.clearFlashMessages();
        if(lf_ready) {
            // draw raw texture on batch if needed
            if (SHOW_SOURCE) {
                int dx = 0;
                for(Texture tex : texture_slots){
                    if (tex != null) {
                        batch.draw(tex, dx, 250, 40, 80*8);
                        dx += 45;
                    }
                }

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
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        tex_control.dispose();
        disposeExistingTexture();
        slotImage.dispose();
        for(Texture tex : texture_slots){
            if(tex != null){
                tex.dispose();
            }
        }
    }

    @Override
    public void disposeExistingTexture(){
        for(int i = 0 ; i<texture_slots.length ; i++){
            Texture tex = texture_slots[i];
            if(tex != null){
                tex.dispose();
            }
            texture_slots[i] = null;
        }
        lf_counter = 0;
        lf_ready = false;
    }

    @Override
    boolean touchDragged (int screenX, int screenY, int pointer){
        Gdx.app.log("Drag:", "X:" + screenX + " , Y:" + screenY);
        screenX = clamp(screenX, 0, Gdx.graphics.getWidth());
        screenY = clamp(screenY, 0, Gdx.graphics.getHeight());
        cameraPositionX = (float)(screenX) / (float)(Gdx.graphics.getWidth());
        cameraPositionY = (float)(screenY) / (float)(Gdx.graphics.getHeight());
        return false;
    }

    private static <T extends Comparable<T>> T clamp(T val, T min, T max){
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }


}
