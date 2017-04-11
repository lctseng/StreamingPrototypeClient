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

import StreamingFormat.Message;

/**
 * Created by lctseng on 2017/2/6.
 * NTU COV-ART Lab, for NCP project
 */

public class DisplayLightField extends DisplayBase{


    final static int COL_WIDTH = 8;
    final static int ROW_WIDTH = 2;
    final static int TOTAL_IMAGES = COL_WIDTH * ROW_WIDTH;
    final static int DIMENSION = 512;

    final static int HALF_COL_SPAN = 0;

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // texture manager
    private TextureManager textureManager;


    private ShaderProgram shaderProgram;

    private Mesh mesh;

    private float focus = 0.0f;
    private float aperture = 5.0f;


    private Texture textureStartStop;
    private Texture textureChangeScene;

    private Matrix4 modelviewMatrix;
    private Matrix4 projectionMatrix;




    DisplayLightField(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        textureStartStop = new Texture("start-stop.png");
        textureChangeScene = new Texture("change-scene.png");

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


    public void collectImages(){
        Buffer src = BufferPool.getInstance().queueDecoderToDisplay.poll();
        if(src != null){
            // copy images from buffer
            textureManager.addImage(src);

            if(!BufferPool.getInstance().queueDisplayToDecoder.offer(src)){
                Gdx.app.error("LightField Display", "Cannot return the buffer to pool");
            }
        }
    }

    @Override
    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        collectImages();







        shaderProgram.begin();
        // set matrix
        shaderProgram.setUniformMatrix("projectionMatrix", projectionMatrix);
        shaderProgram.setUniformMatrix("modelviewMatrix", modelviewMatrix);
        // set camera params
        shaderProgram.setUniformi("rows", ROW_WIDTH);
        shaderProgram.setUniformi("cols", COL_WIDTH);
        shaderProgram.setUniformf("focusPoint", focus);
        shaderProgram.setUniformf("apertureSize", aperture);
        shaderProgram.setUniformf("cameraPositionX", textureManager.getCameraPositionX());
        shaderProgram.setUniformf("cameraPositionY", textureManager.getCameraPositionY());
        //Gdx.app.log("LightField Display", "X: " + cameraPositionX + " , Y: " + cameraPositionY);
        shaderProgram.setUniformi("col_start", textureManager.getColumnStart());
        shaderProgram.setUniformi("col_end", textureManager.getColumnEnd());
        // binding texture
        textureManager.bindTextures(shaderProgram);
        // draw!
        mesh.render(shaderProgram, GL20.GL_TRIANGLES);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        shaderProgram.end();
        Profiler.reportOnDisplay();





        batch.begin();
        // draw control
        batch.draw(textureStartStop, 0, Gdx.graphics.getHeight() - 150, 150, 150);
        batch.draw(textureChangeScene, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 150, 150, 150);
        // clear flash messages
        StringPool.clearFlashMessages();
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
        textureStartStop.dispose();
        textureChangeScene.dispose();
        textureManager.dispose();
    }

    @Override
    public void disposeExistingTexture(){
        textureManager.disposeExistingTextures();
    }

    private static <T extends Comparable<T>> T clamp(T val, T min, T max){
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    @Override
    public void onSensorDataReady(Sensor sensor){
        textureManager.updateDelta(sensor.getTranslationMagnitudeHorz(), sensor.getTranslationMagnitudeVert());
    }

    @Override
    public void attachControlFrameInfo(Message.Control.Builder controlBuilder){
        textureManager.attachControlFrameInfo(controlBuilder);
    }

    @Override
    public boolean checkControlFrameRequired(){
        return textureManager.checkControlFrameRequired();
    }
}
