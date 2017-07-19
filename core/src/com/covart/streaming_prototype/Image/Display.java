package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.android.CardboardCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.covart.streaming_prototype.Buffer;
import com.covart.streaming_prototype.BufferPool;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Profiler;
import com.covart.streaming_prototype.Sensor;
import com.covart.streaming_prototype.StringPool;
import com.covart.streaming_prototype.UI.PositionController;
import com.google.vrtoolkit.cardboard.Eye;

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

    // cameras
    private CardboardCamera camMain;
    //private PerspectiveCamera[] vrCameras;
    private int vrRectWidth;

    // models
    private Model model;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private CameraInputController camController;
    private Environment environment;

    private int vrScreenOffsetsX[];
    private int vrScreenOffsetsY[];
    private int vrEyeIndex;

    private Vector3 initLookAt;

    private Vector3 tmpVector1;

    private Texture texture;

    public Eye currentEye;

    public Display(){

        texture = new Texture("grid.jpg");

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // temps
        tmpVector1 = new Vector3();

        // multi-texture
        textureManager = new TextureManager(this);

        // main camera
        initLookAt = new Vector3(0,0,-1);
        camMain = new CardboardCamera();
        recenterCamera();
        camMain.near = 0.1f;
        camMain.far = ConfigManager.getFocusChangeRatio();

        // VR mode settings
        vrEyeIndex = 0;
        vrRectWidth = Gdx.graphics.getWidth() / 2;
        vrScreenOffsetsX = new int[]{0, vrRectWidth};
        int vrYOffset = (Gdx.graphics.getHeight() - vrRectWidth)/2;
        vrScreenOffsetsY = new int[]{vrYOffset, vrYOffset};
        // VR mode camera
        /*
        vrCameras = new PerspectiveCamera[2];
        for(int i=0;i<2;i++) {
            vrCameras[i] = new PerspectiveCamera(ConfigManager.getVirtualCameraFOV(), vrRectWidth, vrRectWidth);
            vrCameras[i].position.set(camMain.position);
            vrCameras[i].lookAt(initLookAt);
            vrCameras[i].near = camMain.near;
            vrCameras[i].far = camMain.far;
        }
        */

        String vertexShader = Gdx.files.internal("shaders/lightfield_new.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/lightfield_new.frag").readString();
        LightFieldShaderProvider shaderProvider = new LightFieldShaderProvider(vertexShader, fragmentShader);
        shaderProvider.setDisplay(this);

        modelBatch = new ModelBatch(shaderProvider);

        /*
        camController = new CameraInputController(camMain);
        camController.pinchZoomFactor = 1f;
        camController.rotateAngle = 30;
        camController.translateUnits = 1;
        camController.target.set(0,0,-1);
        camController.translateTarget = false;
        camController.forwardTarget = false;
        */



        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        float radius = 10f;

        model = modelBuilder.createRect(
                -radius,-radius,-10,
                radius,-radius,-10,
                radius,radius,-10,
                -radius,radius,-10,
                0,0,1,
                new Material(TextureAttribute.createDiffuse(texture)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
        );
        instance = new ModelInstance(model);
    }

    public void attachInputProcessors(InputMultiplexer inputMultiplexer){
        //inputMultiplexer.addProcessor(camController);
    }

    public void start(){
        disposeExistingTexture();
        textureManager.createTextureSlots(ConfigManager.getNumOfLFs());
    }


    private void collectImages(){
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
        //Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        modelBatch.begin(camMain);
        modelBatch.render(instance, environment);
        modelBatch.end();
    }


    private void updateVRCameras(){
        /*
        // compute right vector of main
        tmpVector1.set(camMain.direction).crs(camMain.up).nor();
        tmpVector1.scl(ConfigManager.getDisplayVRDisparity());
        // update cameras
        for(int i=0;i<2;i++) {
            vrCameras[i].fieldOfView = camMain.fieldOfView;
            vrCameras[i].far = camMain.far;
            vrCameras[i].position.set(camMain.position);
            vrCameras[i].direction.set(camMain.direction);
            vrCameras[i].up.set(camMain.up);
            if(i == 0){
                vrCameras[i].translate(-tmpVector1.x, -tmpVector1.y, -tmpVector1.z);
            }
            else{
                vrCameras[i].translate(tmpVector1.x, tmpVector1.y, tmpVector1.z);
            }
            vrCameras[i].update();
        }
        */
    }

    private void drawVRView(){
        /*
        updateVRCameras();
        for(int i=0;i<2;i++){
            vrEyeIndex = i;
            // for VR mode, we use width == height now
            Gdx.gl.glViewport(i * vrRectWidth, (Gdx.graphics.getHeight() - vrRectWidth)/2, vrRectWidth, vrRectWidth);
            modelBatch.begin(vrCameras[i]);
            modelBatch.render(instance, environment);
            modelBatch.end();
        }
        */
    }

    public void updateStart(){
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

        collectImages();

        //camMain.far = ConfigManager.getFocusChangeRatio();
        //camMain.fieldOfView = ConfigManager.getVirtualCameraFOV();
        //camMain.update();

        //camController.update();

        // Render
        switch(ConfigManager.getDisplayMode()){
            case NORMAL:
                drawNormalView();
                break;
            case VR:
                drawVRView();
                break;
        }
        Profiler.reportOnDisplay();

        //Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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

    public void recenterCamera(){
        camMain.position.set(0f, 0f, 3f);
        camMain.up.set(0,1,0);
        camMain.lookAt(initLookAt);
        camMain.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        textureManager.dispose();
        model.dispose();
        modelBatch.dispose();
    }

    private void disposeExistingTexture(){
        textureManager.disposeExistingTextures();
    }

    private static <T extends Comparable<T>> T clamp(T val, T min, T max){
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    public void onSensorDataReady(Sensor sensor){
        // update texture manager
        //textureManager.updateDelta(sensor.getTranslationMagnitudeHorz(), sensor.getTranslationMagnitudeVert());
        sensor.makeRotateDiff();
        StringPool.addField("Rotate Diff", "Horz:" + sensor.getHorzRotateDiff() + ", Vert:" + sensor.getVertRotateDiff());
        tmpVector1.set(camMain.direction).crs(camMain.up).y = 0f;
        camMain.rotateAround(camController.target, tmpVector1.nor(), sensor.getVertRotateDiff() * ConfigManager.getSensorRotationToCameraRatio());
        camMain.rotateAround(camController.target, Vector3.Y, sensor.getHorzRotateDiff() * ConfigManager.getSensorRotationToCameraRatio());
    }

    public void attachControlFrameInfo(Message.Control.Builder controlBuilder){
        textureManager.attachControlFrameInfo(controlBuilder);
    }

    public boolean checkControlFrameRequired(){
        return textureManager.checkControlFrameRequired();
    }


    public TextureManager getTextureManager() {
        return textureManager;
    }

    public int getScreenWidth(){
        return vrRectWidth;
        /*
        if(ConfigManager.getDisplayMode() == Mode.VR){
            return vrRectWidth;
        }
        else{
            return Gdx.graphics.getWidth();
        }
        */

    }

    public int getScreenHeight(){
        return vrRectWidth;
        /*
        if(ConfigManager.getDisplayMode() == Mode.VR){
            return vrRectWidth;
        }
        else{
            return Gdx.graphics.getHeight();
        }
        */

    }

    public int getScreenOffsetX(){
        if(currentEye.getType() == Eye.Type.LEFT){
            return vrScreenOffsetsX[0];
        }
        else{
            return vrScreenOffsetsX[1];
        }
        /*
        if(ConfigManager.getDisplayMode() == Mode.VR){
            return vrScreenOffsetsX[vrEyeIndex];
        }
        else{
            return 0;
        }
        */
    }

    public int getScreenOffsetY(){
        if(currentEye.getType() == Eye.Type.LEFT){
            return vrScreenOffsetsY[0];
        }
        else{
            return vrScreenOffsetsY[1];
        }
        /*
        if(ConfigManager.getDisplayMode() == Mode.VR){
            return vrScreenOffsetsY[vrEyeIndex];
        }
        else{
            return 0;
        }
        */
    }

    public boolean getEnableDistortionCorrection(){
        return ConfigManager.getDisplayMode() == Display.Mode.VR;
    }

    public CardboardCamera getMainCamera(){
        return camMain;
    }

    public void manuallyMoveCamera(PositionController.Direction direction){
        switch(direction){
            case LEFT:
                camMain.translate(-ConfigManager.getManuallyMoveStep(), 0, 0);
                break;
            case RIGHT:
                camMain.translate(ConfigManager.getManuallyMoveStep(), 0, 0);
                break;
            case UP:
                camMain.translate(0, ConfigManager.getManuallyMoveStep(), 0);
                break;
            case DOWN:
                camMain.translate(0, -ConfigManager.getManuallyMoveStep(), 0);
                break;

        }
    }
}
