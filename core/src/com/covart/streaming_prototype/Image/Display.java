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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.covart.streaming_prototype.Buffer;
import com.covart.streaming_prototype.BufferPool;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Profiler;
import com.covart.streaming_prototype.StringPool;
import com.covart.streaming_prototype.UI.PositionController;
import com.covart.streaming_prototype.UI.UIManager;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;

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
    private int vrRectWidth;

    // models
    private Model model;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private Environment environment;

    private Vector3 initLookAt;

    private Vector3 tmpVector1;

    private Texture texture;

    public Eye currentEye;
    public Matrix4 rfrdProjection;

    public Display(){

        texture = new Texture("grid.jpg");

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // temps
        tmpVector1 = new Vector3();
        rfrdProjection = new Matrix4();

        // multi-texture
        textureManager = new TextureManager(this);

        // main camera
        initLookAt = new Vector3(0,0,-1);
        camMain = new CardboardCamera();
        recenterCamera();
        camMain.near = 0.1f;
        camMain.far = ConfigManager.getFocusChangeRatio();

        // VR mode settings
        vrRectWidth = Gdx.graphics.getWidth() / 2;

        String vertexShader = Gdx.files.internal("shaders/lightfield_new.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/lightfield_new.frag").readString();
        LightFieldShaderProvider shaderProvider = new LightFieldShaderProvider(vertexShader, fragmentShader);
        shaderProvider.setDisplay(this);

        modelBatch = new ModelBatch(shaderProvider);


        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        float radius = 10f;
        float depth = -5f;

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

        currentEye = eye;
        // update camera for this eye
        Matrix4 eyeMatrix = new Matrix4(eye.getEyeView());
        camMain.setEyeViewAdjustMatrix(eyeMatrix);


        float[] perspective = eye.getPerspective(0.1f, ConfigManager.getFocusChangeRatio());
        camMain.setEyeProjection(new Matrix4(perspective));
        camMain.update();

        // draw!
        modelBatch.begin(camMain);
        modelBatch.render(instance, environment);
        modelBatch.end();

        // only draw for LEFT eye or MONOCULAR
        if(eye.getType() == Eye.Type.LEFT || eye.getType() == Eye.Type.MONOCULAR) {
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
        for(String text : StringPool.getAllText()){
            font.draw(batch, text, 0, dy);
            dy += 20;
        }
        batch.end();
    }

    public void onNewFrame(HeadTransform paramHeadTransform) {
        collectImages();
        Profiler.reportOnDisplay();
        StringPool.clearFlashMessages();
    }

    public void onFinishFrame(com.google.vrtoolkit.cardboard.Viewport paramViewport) {
        // record FPS
        StringPool.addField("FPS", Integer.toString(Gdx.graphics.getFramesPerSecond()));
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
            case FORWARD:
                camMain.translate(0, 0, -ConfigManager.getManuallyMoveStep());
                break;
            case BACKWARD:
                camMain.translate(0, 0, ConfigManager.getManuallyMoveStep());
                break;

        }
    }
}
