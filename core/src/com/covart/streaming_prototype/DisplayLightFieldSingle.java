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

public class DisplayLightFieldSingle extends DisplayBase{


    final static int GRID_WIDTH = 8;
    final static int TOTAL_IMAGES = GRID_WIDTH * GRID_WIDTH;
    final static int DIMENSION = 512;

    final static boolean SHOW_SOURCE = false;

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // buffers
    private Pixmap image;
    private ByteBuffer imageBuf;
    private Texture texture;


    private ShaderProgram shaderProgram;
    private int lf_counter;

    private Mesh mesh;

    private boolean lf_ready;

    private float focus = 0.0f;
    private float aperture = 5.0f;
    private float cameraPositionX =0.5f;
    private float cameraPositionY =0.5f;


    private Texture tex_control;


    DisplayLightFieldSingle(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        image = new Pixmap(DIMENSION * GRID_WIDTH, DIMENSION * GRID_WIDTH, Pixmap.Format.RGB888);
        imageBuf = image.getPixels();
        texture = null;

        tex_control = new Texture("badlogic.jpg");

        String vertexShader = Gdx.files.internal("shaders/lightfield_single.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/lightfield_single.frag").readString();
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        Gdx.app.error("GLSL", "Compiling:" + shaderProgram.isCompiled());
        Gdx.app.error("GLSL", shaderProgram.getLog());
        for(String str : shaderProgram.getUniforms()){
            Gdx.app.error("GLSL", "Uniform:" + str);
        }
        shaderProgram.setUniformf("focusPoint", focus);
        shaderProgram.setUniformf("apertureSize", aperture);
        shaderProgram.setUniformi("rows", GRID_WIDTH);
        shaderProgram.setUniformi("cols", GRID_WIDTH);


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
                    lf_ready = true;
                }
            }
        }



        if(lf_ready && !SHOW_SOURCE && texture != null){
            // interpolate LF
            texture.bind();
            shaderProgram.begin();
            Matrix4 modelviewMatrix = new Matrix4();
            Matrix4 projectionMatrix = new Matrix4();
            projectionMatrix.setToOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
            // set matrix
            shaderProgram.setUniformMatrix("projectionMatrix", projectionMatrix);
            shaderProgram.setUniformMatrix("modelviewMatrix", modelviewMatrix);
            // set camera params
            shaderProgram.setUniformi("rows", GRID_WIDTH);
            shaderProgram.setUniformi("cols", GRID_WIDTH);
            shaderProgram.setUniformf("focusPoint", focus);
            shaderProgram.setUniformf("apertureSize", aperture);
            shaderProgram.setUniformf("cameraPositionX", cameraPositionX);
            shaderProgram.setUniformf("cameraPositionY", cameraPositionY);
            Gdx.app.log("LightField Display", "X: " + cameraPositionX + " , Y: " + cameraPositionY);
            // draw!
            mesh.render(shaderProgram, GL20.GL_TRIANGLES);
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
                if (texture != null) {
                    batch.draw(texture, 0, 250, 700, 700);
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
        tex_control.dispose();
        disposeExistingTexture();
    }

    @Override
    public void disposeExistingTexture(){
        if (texture != null){
            texture.dispose();
            texture = null;
        }
        lf_counter = 0;
        lf_ready = false;
    }

    private static <T extends Comparable<T>> T clamp(T val, T min, T max){
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }


    @Override
    public void onSensorDataReady(Sensor sensor){
        // map direction into cx cy
        cameraPositionX = (float) (sensor.getRotation().getYaw() / 360.0 + 0.5);
        cameraPositionY = (float) (sensor.getRotation().getPitch() / -180.0 + 0.5);
    }
}
