package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * Created by lctseng on 2017/2/6.
 * NTU COV-ART Lab, for NCP project
 */

public class DisplayLightField extends DisplayBase{


    final static int COL_WIDTH = 6;
    final static int ROW_WIDTH = 2;
    final static int TOTAL_IMAGES = COL_WIDTH * ROW_WIDTH;
    final static int DIMENSION = 512;

    final static boolean SHOW_SOURCE = false;

    final static int HALF_COL_SPAN = 10;

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // texture manager
    private TextureManager textureManager;


    private ShaderProgram shaderProgram;
    private int lf_counter;

    private Mesh mesh;

    private boolean lf_ready;

    private float focus = 0.0f;
    private float aperture = 5.0f;
    private float cameraPositionX =0.5f;
    private float cameraPositionY =0.5f;


    private Texture tex_control;

    private Matrix4 modelviewMatrix;
    private Matrix4 projectionMatrix;


    DisplayLightField(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        tex_control = new Texture("badlogic.jpg");

        // multi-texture
        textureManager = new TextureManager(this);


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


        // create matrix
        modelviewMatrix = new Matrix4();
        projectionMatrix = new Matrix4();
        float ratio = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        projectionMatrix.setToOrtho(-1.0f, 1.0f, ratio * -1, ratio, -1.0f, 1.0f);


        // create mesh
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
    void start(){
        disposeExistingTexture();
        textureManager.createTextureSlots(COL_WIDTH);
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
                src.index = col;
                textureManager.addImage(src, row);

                ++lf_counter;
                Gdx.app.log("LightField Display", "Loading light field:" + lf_counter);
                if(!BufferPool.getInstance().queueDisplayToDecoder.offer(src)){
                    Gdx.app.error("LightField Display", "Cannot return the buffer to pool");
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
                textureManager.getTextures()[i].bind(i);
                shaderProgram.setUniformi("u_custom_texture" + i, i);
            }

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
            //Gdx.app.log("LightField Display", "X: " + cameraPositionX + " , Y: " + cameraPositionY);
            // compute column start/end
            int cameraIdxX = (int)(cameraPositionX * COL_WIDTH);
            int col_start = cameraIdxX - HALF_COL_SPAN;
            int col_end = cameraIdxX + HALF_COL_SPAN + 1;
            if(col_start < 0) col_start = 0;
            if(col_start >= COL_WIDTH) col_start = COL_WIDTH - 1;
            if(col_end > COL_WIDTH) col_end = COL_WIDTH ;
            shaderProgram.setUniformi("col_start", col_start);
            shaderProgram.setUniformi("col_end", col_end);
            //Gdx.app.log("LightField Display", "Effective col: " + col_start + "-" + (col_end-1));
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
                for(Texture tex : textureManager.getTextures()){
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
        textureManager.dispose();
    }

    @Override
    public void disposeExistingTexture(){
        textureManager.disposeExistingTextures();
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
        cameraPositionX = (float) (sensor.getTranslationMagnitudeHorz() + 0.5);
        cameraPositionY = (float) (sensor.getTranslationMagnitudeVert() + 0.5);
    }




}
