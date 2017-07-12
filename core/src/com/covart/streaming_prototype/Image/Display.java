package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;
import com.covart.streaming_prototype.Buffer;
import com.covart.streaming_prototype.BufferPool;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Profiler;
import com.covart.streaming_prototype.Sensor;
import com.covart.streaming_prototype.StringPool;
import com.covart.streaming_prototype.UI.UIManager;

import StreamingFormat.Message;


/**
 * Created by lctseng on 2017/2/6.
 * NTU COV-ART Lab, for NCP project
 */

public class Display implements Disposable{

    public enum Mode {
        VR, NORMAL
    }

    // gdx basic drawing
    private SpriteBatch batch;
    private BitmapFont font;

    // texture manager
    private TextureManager textureManager;

    private PerspectiveCamera cam;
    private Model model;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private CameraInputController camController;
    private Environment environment;

    private LightFieldShaderProvider shaderProvider;

    private float lastScreenX;
    private float lastScreenY;

    public Display(){

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // multi-texture
        textureManager = new TextureManager(this);


        String vertexShader = Gdx.files.internal("shaders/lightfield_new.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/lightfield_new.frag").readString();

        // create VR two-eye projection
        // FIXME: New VR mode
        /*
        int vrRectWidth;
        vrRectWidth = Gdx.graphics.getWidth() / 2;
        float vrRatio = Gdx.graphics.getHeight() / (float)vrRectWidth;
        projectionMatrixLeft.setToOrtho(-1.0f, 3.0f , -1.0f * vrRatio, vrRatio, -1.0f, 1.0f);
        projectionMatrixRight.setToOrtho(-3.0f, 1.0f, -1.0f * vrRatio, vrRatio, -1.0f, 1.0f);
        */

        cam = new PerspectiveCamera(ConfigManager.getVirtualCameraFOV(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 0f, 3f);
        cam.lookAt(0,0,-1);
        cam.near = 0.1f;
        cam.far = ConfigManager.getFocusChangeRatio();
        cam.update();



        shaderProvider = new LightFieldShaderProvider(vertexShader, fragmentShader);
        shaderProvider.setTextureManager(textureManager);

        modelBatch = new ModelBatch(shaderProvider);

        camController = new CameraInputController(cam);
        camController.pinchZoomFactor = 1f;
        camController.rotateAngle = 30;
        camController.translateUnits = 1;
        camController.target.set(0,0,-1);
        camController.translateTarget = false;
        camController.forwardTarget = false;

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(UIManager.getInstance().getInputProcessor());

        InputAdapter localInput = new InputAdapter() {
            @Override
            public boolean touchDown (int screenX, int screenY, int pointer, int button) {
                if(ConfigManager.getSensorMoveType() == Sensor.MoveType.MANUAL){
                    lastScreenX = screenX;
                    lastScreenY = screenY;
                    return true;
                }
                else{
                    return false;
                }

            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(ConfigManager.getSensorMoveType() == Sensor.MoveType.MANUAL){
                    // do move
                    float dx = -(screenX - lastScreenX) / Gdx.graphics.getWidth();
                    float dy = (screenY - lastScreenY) / Gdx.graphics.getHeight();
                    cam.translate(dx, dy, 0);
                    // update  screen XY
                    lastScreenX = screenX;
                    lastScreenY = screenY;

                    return true;
                }
                else {
                    return false;
                }
            }
        };
        inputMultiplexer.addProcessor(localInput);
        inputMultiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(inputMultiplexer);


        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        float radius = 10f;

        model = modelBuilder.createRect(
                -radius,-radius,0,
                radius,-radius,0,
                radius,radius,0,
                -radius,radius,0,
                0,0,radius,
                new Material(),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
        );
        instance = new ModelInstance(model);
    }

    public void start(){
        disposeExistingTexture();
        textureManager.createTextureSlots(ConfigManager.getNumOfLFs());
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

    private void drawNormalView(){
        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }


    private void drawVRView(){
        /*
        float disparity = ConfigManager.getDisplayVRDisparity();
        float centerX = textureManager.getCameraPositionX();

        // compute left and right X
        float leftX = centerX - disparity;
        float rightX = centerX + disparity;

        // draw left eye
        shaderProgram.begin();
        // set matrix
        shaderProgram.setUniformMatrix("projectionMatrix", projectionMatrixLeft);
        shaderProgram.setUniformMatrix("modelviewMatrix", modelviewMatrix);
        // set camera params
        shaderProgram.setUniformi("rows", ConfigManager.getNumOfSubLFImgs());
        shaderProgram.setUniformi("cols", ConfigManager.getNumOfLFs());
        shaderProgram.setUniformf("apertureSize", ConfigManager.getApertureSize());
        shaderProgram.setUniformf("cameraPositionX", leftX);
        shaderProgram.setUniformf("cameraPositionY", textureManager.getCameraPositionY());
        shaderProgram.setUniformi("col_start", textureManager.getColumnStart());
        shaderProgram.setUniformi("col_end", textureManager.getColumnEnd());
        shaderProgram.setUniformi("enable_distortion_correction", 1);
        shaderProgram.setUniformf("lensFactorX", ConfigManager.getDisplayLensFactorX());
        shaderProgram.setUniformf("lensFactorY", ConfigManager.getDisplayLensFactorY());
        // binding texture
        textureManager.bindTextures(shaderProgram);
        // draw!
        //mesh.render(shaderProgram, GL20.GL_TRIANGLES);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        shaderProgram.end();

        // draw right eye
        shaderProgram.begin();
        // set matrix
        shaderProgram.setUniformMatrix("projectionMatrix", projectionMatrixRight);
        shaderProgram.setUniformMatrix("modelviewMatrix", modelviewMatrix);
        // set camera params
        shaderProgram.setUniformi("rows", ConfigManager.getNumOfSubLFImgs());
        shaderProgram.setUniformi("cols", ConfigManager.getNumOfLFs());
        shaderProgram.setUniformf("apertureSize", ConfigManager.getApertureSize());
        shaderProgram.setUniformf("cameraPositionX", rightX);
        shaderProgram.setUniformf("cameraPositionY", textureManager.getCameraPositionY());
        shaderProgram.setUniformi("col_start", textureManager.getColumnStart());
        shaderProgram.setUniformi("col_end", textureManager.getColumnEnd());
        shaderProgram.setUniformi("enable_distortion_correction", 1);
        shaderProgram.setUniformf("lensFactorX", ConfigManager.getDisplayLensFactorX());
        shaderProgram.setUniformf("lensFactorY", ConfigManager.getDisplayLensFactorY());
        // binding texture
        textureManager.bindTextures(shaderProgram);
        // draw!
        //mesh.render(shaderProgram, GL20.GL_TRIANGLES);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        shaderProgram.end();
        */
    }

    public void updateStart(){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

        collectImages();

        cam.far = ConfigManager.getFocusChangeRatio();
        cam.fieldOfView = ConfigManager.getVirtualCameraFOV();
        cam.update();
        StringPool.addField("Far", "" + cam.far);

        camController.update();
        switch(ConfigManager.getDisplayMode()){
            case NORMAL:
                drawNormalView();
                break;
            case VR:
                drawVRView();
                break;
        }
        Profiler.reportOnDisplay();





        batch.begin();
        // clear flash messages
        StringPool.clearFlashMessages();
    }


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
    public void dispose() {
        batch.dispose();
        font.dispose();
        textureManager.dispose();
        model.dispose();
        modelBatch.dispose();
    }

    public void disposeExistingTexture(){
        textureManager.disposeExistingTextures();
    }

    private static <T extends Comparable<T>> T clamp(T val, T min, T max){
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    public void onSensorDataReady(Sensor sensor){
        // update texture manager
        textureManager.updateDelta(sensor.getTranslationMagnitudeHorz(), sensor.getTranslationMagnitudeVert());
    }

    public void attachControlFrameInfo(Message.Control.Builder controlBuilder){
        textureManager.attachControlFrameInfo(controlBuilder);
    }

    public boolean checkControlFrameRequired(){
        return textureManager.checkControlFrameRequired();
    }

}
