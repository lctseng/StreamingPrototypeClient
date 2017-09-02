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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.covart.streaming_prototype.Buffer;
import com.covart.streaming_prototype.BufferPool;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Utils.Profiler;
import com.covart.streaming_prototype.StringPool;
import com.covart.streaming_prototype.UI.PositionController;
import com.covart.streaming_prototype.UI.UIManager;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;

import java.util.ArrayList;
import java.util.List;

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

    // models
    private Model model;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private Environment environment;

    private Vector3 initLookAt;

    private Vector3 tmpVector1;
    private Matrix4 tmpMatrix1;

    private Texture texture;

    public EyeWrapper eyeWrapper;

    public Vector2 editingScreenPosition;
    public Vector2 editingImagePosition;

    public List<Vector2> editingImagePositions;

    private boolean editingPositionFollowCursor;

    private long lastStPlaneUpdateTime = 0;
    private boolean requestChangeStPlane = false;

    private boolean drawOverlay = true;

    private long frameStartTime;

    public Display(){

        texture = new Texture("grid.jpg");

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // misc
        editingScreenPosition = new Vector2(-1,-1);
        editingImagePosition = new Vector2(-1,-1);
        editingImagePositions = new ArrayList<Vector2>();

        // temps
        tmpVector1 = new Vector3();
        tmpMatrix1 = new Matrix4();

        // multi-texture
        textureManager = new TextureManager(this);

        // main camera
        initLookAt = new Vector3(0,0,-1);
        camMain = new CardboardCamera();
        recenterCamera();
        camMain.near = 0.1f;
        camMain.far = ConfigManager.getFocusChangeRatio();

        eyeWrapper = new EyeWrapper(camMain);

        // VR mode settings

        String vertexShader = Gdx.files.internal("shaders/lightfield_new.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/lightfield_new.frag").readString();
        LightFieldShaderProvider shaderProvider = new LightFieldShaderProvider(vertexShader, fragmentShader);
        shaderProvider.setDisplay(this);

        modelBatch = new ModelBatch(shaderProvider);


        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        createStPlane();
    }

    public void refreshStPlane(){
        requestChangeStPlane = true;
    }

    private void updateCheckUpdateStPlane(){
        long now = System.nanoTime();
        if(now - lastStPlaneUpdateTime > 100000000){
            destroyStPlane();
            createStPlane();
            lastStPlaneUpdateTime = now;
            requestChangeStPlane = false;
        }
    }

    public void destroyStPlane(){
        if(model != null) {
            model.dispose();
            model = null;
        }
        instance = null;
    }

    public void createStPlane(){
        float radius = ConfigManager.getStPlaneRadius();
        float depth = 0;

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createRect(
                -radius,-radius,depth,
                radius,-radius,depth,
                radius,radius,depth,
                -radius,radius,depth,
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

    public void onDrawEye(Eye eye) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);


        eyeWrapper.setEye(eye);
        eyeWrapper.inspect();
        // update camera for this eye
        // eye view matrix
        tmpMatrix1.set(eyeWrapper.getEyeView());
        camMain.setEyeViewAdjustMatrix(tmpMatrix1);

        // projection matrix
        tmpMatrix1.set(eyeWrapper.getPerspective(0.01f, 100f));
        camMain.setEyeProjection(tmpMatrix1);
        camMain.update();

        // draw!
        modelBatch.begin(camMain);
        modelBatch.render(instance, environment);
        modelBatch.end();

        // only draw for LEFT eye or MONOCULAR
        if(drawOverlay && ConfigManager.isMainEye(eye)) {
            drawOverlay();
        }
    }

    private void drawOverlay(){
        // draw UI
        UIManager.getInstance().draw();
        // draw text
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        int dy = 20;
        for (String text : StringPool.getAllText()) {
            font.draw(batch, text, 0, dy);
            dy += 20;
        }
        batch.end();
    }

    public void onNewFrame(HeadTransform paramHeadTransform) {
        if(requestChangeStPlane){
            updateCheckUpdateStPlane();
        }
        eyeWrapper.onNewFrame();
        collectImages();
        Profiler.reportOnDisplay();
        StringPool.clearFlashMessages();
        frameStartTime = System.nanoTime();
    }

    public void onFinishFrame(com.google.vrtoolkit.cardboard.Viewport paramViewport) {
        long frameEndTime = System.nanoTime();
        long currentFrameDrawTime = (frameEndTime - frameStartTime) / 1000000;

        long needSleepTime = ConfigManager.getDisplayMinDrawTime() - currentFrameDrawTime;
        if(needSleepTime > 0){
            try {
                Thread.sleep(needSleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // record FPS
        StringPool.addField("FPS", Integer.toString(Gdx.graphics.getFramesPerSecond()));
    }
    public void recenterCamera(){
        camMain.position.set(0f, 0f, 3f);
        camMain.up.set(0,-1,0);
        camMain.lookAt(initLookAt);
        camMain.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        textureManager.dispose();
        if(model != null) {
            model.dispose();
        }
        modelBatch.dispose();
    }

    private void disposeExistingTexture(){
        textureManager.disposeExistingTextures();
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


    public CardboardCamera getMainCamera(){
        return camMain;
    }

    public void manuallyMoveCamera(PositionController.Direction direction){
        moveCamera(direction, ConfigManager.getManuallyMoveStep());
    }

    public void moveCamera(PositionController.Direction direction, float distance){
        switch(direction){
            case LEFT:
                camMain.translate(distance, 0, 0);
                break;
            case RIGHT:
                camMain.translate(-distance, 0, 0);
                break;
            case UP:
                camMain.translate(0, -distance, 0);
                break;
            case DOWN:
                camMain.translate(0, distance, 0);
                break;
            case FORWARD:
                camMain.translate(0, 0, -distance);
                break;
            case BACKWARD:
                camMain.translate(0, 0, distance);
                break;
        }
    }

    public void updateEditingScreenPosition(float screenX, float screenY){
        // compute ratio
        float ratioX = screenX / Gdx.graphics.getWidth();
        float ratioY = 1f - screenY / Gdx.graphics.getHeight(); // Y is reversed
        editingScreenPosition.set(eyeWrapper.getViewport().width * ratioX, eyeWrapper.getViewport().height * ratioY);
    }


    public Eye currentEye(){
        return this.eyeWrapper.getEye();
    }

    public Vector3 getLastEyePosition(){
        return this.eyeWrapper.getLastEyePosition();
    }

    // Assume model list is ready
    // TODO: image coordinate should read from list
    public void prepareForEditingMode(){

        editingImagePositions.clear();
        // TODO: copy image coordinate from model list
        for(int i=0;i<ConfigManager.getEditingModelIdList().size();i++){
            editingImagePositions.add(new Vector2(i * 10,ConfigManager.getImageHeight()/2));
        }
    }

    public void startEditingModel(){
        editingImagePosition.set(editingImagePositions.get(ConfigManager.getEditingCurrentModelIndex()));
        editingPositionFollowCursor = false;
    }

    public void finishEditingModel(int lastIndex){
        // save image position
        editingImagePositions.get(lastIndex).set(editingImagePosition);
        editingImagePosition.set(-1, -1);
        editingPositionFollowCursor = false;
    }

    public boolean isEditingPositionFollowCursor() {
        return editingPositionFollowCursor;
    }

    public void setEditingPositionFollowCursor(boolean editingPositionFollowCursor) {
        this.editingPositionFollowCursor = editingPositionFollowCursor;
    }

    public boolean isDrawOverlay() {
        return drawOverlay;
    }

    public void setDrawOverlay(boolean drawOverlay) {
        this.drawOverlay = drawOverlay;
    }
}
