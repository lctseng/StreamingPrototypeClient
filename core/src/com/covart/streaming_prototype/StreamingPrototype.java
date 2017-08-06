package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.Image.Display;
import com.covart.streaming_prototype.Image.ImageDecoderBase;
import com.covart.streaming_prototype.Image.ImageDecoderStaticFiles;
import com.covart.streaming_prototype.Net.Network;
import com.covart.streaming_prototype.UI.MainMenu;
import com.covart.streaming_prototype.UI.PositionController;
import com.covart.streaming_prototype.UI.UIManager;
import com.covart.streaming_prototype.Utils.Profiler;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;

import java.util.Locale;

import StreamingFormat.Message;

import static com.badlogic.gdx.Gdx.app;
import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.covart.streaming_prototype.StreamingPrototype.State.Running;
import static com.covart.streaming_prototype.StreamingPrototype.State.ShuttingDown;
import static com.covart.streaming_prototype.StreamingPrototype.State.Stopped;

public class StreamingPrototype extends ApplicationAdapter
        implements MasterComponentAdapter, CardBoardApplicationListener {


    public enum State {
        Stopped, Running, ShuttingDown
    }

    CardBoardAndroidApplication cardBoardApp;

    private State state = Stopped;
    private volatile boolean startRequired = false;
    private volatile boolean stopRequired = false;

    // major component
    public Display display;
    public Network network;
    public ImageDecoderBase decoder;

    private float sensorSendDataTime;

    // change scene
    public boolean sceneChanged = false;

    private boolean saveFrameRequested = false;

    // editing
    private float editingReportTime;

    // UI
    public PositionController positionController;


    StreamingPrototype(ImageDecoderBase platform_decoder) {
        if (platform_decoder != null) {
            decoder = platform_decoder;
        }
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
        UIManager.getInstance().onAppStateChanged();
    }

    @Override
    public void resize(int width, int height) {
        UIManager.getInstance().resetViewport();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void onNewFrame(HeadTransform paramHeadTransform) {
        display.onNewFrame(paramHeadTransform);
        //Profiler.generateProfilingStrings();

        if (startRequired) {
            start();
        }
        if (stopRequired) {
            stop();
        }
        sensorSendDataTime += Gdx.graphics.getDeltaTime();
        if (sensorSendDataTime > ConfigManager.getSensorReportInterval()) {
            sensorSendDataTime = 0f;
            sendSenserData();
        }

        // manually move
        if(ConfigManager.isEnableManuallyMove() && ConfigManager.getCurrentMoveDirection() != PositionController.Direction.NONE){
            manuallyMoveCamera(ConfigManager.getCurrentMoveDirection());
        }

        // control frame update if started
        if (state == Running) {
            updateEditing();
            updateControlFrame();
        }
    }

    @Override
    public boolean isVRModeEnabled() {
        return ConfigManager.getDisplayMode() == Display.Mode.VR;
    }

    @Override
    public void onDrawEye(Eye eye) {
        display.onDrawEye(eye);
    }

    @Override
    public void onFinishFrame(com.google.vrtoolkit.cardboard.Viewport paramViewport) {
        display.onFinishFrame(paramViewport);
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    public void onCardboardTrigger() {

    }


    @Override
    public void create() {
        ConfigManager.setApp(this);

        StringPool.addField("App", "Initializing");
        UIManager.initialize();
        network = new Network(this);
        display = new Display();

        if (decoder == null) {
            Gdx.app.error("App", "No platform decoder specified! Use static decoder instead!");
            decoder = new ImageDecoderStaticFiles();
        }

        initializeInput();

        // Setup UI
        positionController = new PositionController();
        UIManager.getInstance().registerUI(new MainMenu());
        UIManager.getInstance().registerUI(positionController);

        StringPool.addField("App", "Ready for start");
        updateEditingModeText();
    }

    private void initializeInput() {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(UIManager.getInstance().getInputProcessor());

        InputAdapter localInput = new InputAdapter() {
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (ConfigManager.isEditingModeEnabled()) {
                    return editingTouchDragged(screenX, screenY, pointer);
                } else {
                    return false;
                }
            }
        };
        inputMultiplexer.addProcessor(localInput);
        display.attachInputProcessors(inputMultiplexer);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void initCardboardApplication(CardBoardAndroidApplication app){
        this.cardBoardApp = app;
    }


    @Override
    public void start() {
        editingReportTime = 0f;
        sensorSendDataTime = 0f;
        stopRequired = false;
        startRequired = false;
        sceneChanged = true;
        app.log("App", "starting");
        StringPool.addField("App", "Component started");
        setState(Running);
        BufferPool.getInstance().reset();
        Profiler.reset();
        display.start();
        network.start();
        decoder.start();
        StringPool.addField("App", "Running. Touch the screen to stop");
    }

    @Override
    public void stop() {
        stopRequired = false;
        startRequired = false;
        app.log("App", "stopping");
        setState(Stopped);
        decoder.stop();
        network.stop();
        StringPool.addField("App", "Stopped. Touch the screen to start the components");
    }

    @Override
    public void requireStop() {
        stopRequired = true;
    }

    @Override
    public void requireStart() {
        startRequired = true;
    }

    @Override
    public void render() {

    }

    private void updateControlFrame() {
        if (checkControlFrameRequired()) {
            sceneChanged = false;
            // create message builder
            Message.Control.Builder controlBuilder = Message.Control.newBuilder();
            // save change scene
            controlBuilder.setChangeScene(ConfigManager.getSceneIndex());
            // save save frame
            if (saveFrameRequested) {
                controlBuilder.setSaveFrame(1);
                saveFrameRequested = false;
            }
            // save drop index
            display.attachControlFrameInfo(controlBuilder);
            // create message
            Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                    .setType(Message.MessageType.MsgControl)
                    .setControlMsg(controlBuilder.build()
                    ).build();
            // send!
            network.sendMessageProtobufAsync(msg);
        }
    }


    private boolean checkControlFrameRequired() {
        // check drop index
        if (display.checkControlFrameRequired() || sceneChanged || saveFrameRequested) {
            return true;
        }
        return false;
    }


    @Override
    public void dispose() {
        UIManager.cleanup();
        decoder.dispose();
        display.dispose();
        network.dispose();
    }
    
    private Message.StreamingMessage makeSensorPacket(Vector3 position, Vector3 direction) {
        // setup builder
        StringPool.addField("Position to Server", position.toString());

        Message.Camera.Builder cameraBuilder = Message.Camera.newBuilder()
                .setDeltaX(clamp(position.x / 1.5f, -1.0f, 1.0f))
                .setDeltaY(clamp(position.y / 1.5f, -1.0f, 1.0f))
                .setDeltaZ(position.z)
                .setDeltaVx(direction.x)
                .setDeltaVy(direction.y)
                .setDeltaVz(direction.z)
                .setSerialNumber(0);

       // crafting packet
        Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                .setType(Message.MessageType.MsgCameraInfo)
                .setCameraMsg(cameraBuilder.build()

                ).build();
        return msg;
    }


    public void sendSenserData() {
        // FIXME: direction should change to Eye direction
        // Can be postponed, because server do not take the direction
        Message.StreamingMessage msg = makeSensorPacket(display.lastEyePosition, display.getMainCamera().direction);
        if (msg != null) {
            network.sendMessageProtobufAsync(msg);
        }
    }

    @Override
    public void dispatchMessage(Message.StreamingMessage msg) throws InterruptedException {
        switch (msg.getType()) {
            case MsgDefaultPos:
                // FIXME: These init position and data need to be applied on GVR?
                Gdx.app.log("Dispatch", "DefaultPos set");
                Message.DefaultPos posMsg = msg.getDefaultPosMsg();
                //sensor.setInitPosition(posMsg.getX(), posMsg.getY(), posMsg.getZ());
                //sensor.setInitDirection(posMsg.getVx(), posMsg.getVy(), posMsg.getVz());
                break;
            case MsgImage:
                int size = msg.getImageMsg().getByteSize();
                Profiler.reportOnRecvStart();
                while (size > 0) {
                    int expectSize;
                    if (size > ConfigManager.getDecoderBufferSize()) {
                        expectSize = ConfigManager.getDecoderBufferSize();
                    } else {
                        // not enough
                        expectSize = size;
                    }
                    size -= expectSize;
                    // acquire new buffer
                    Buffer bufData = BufferPool.getInstance().queueDecoderToNetwork.take();
                    // fill-in content
                    network.getConnection().readn(bufData.data, expectSize);
                    // fill meta data
                    bufData.size = expectSize;
                    bufData.index = msg.getImageMsg().getStatus();
                    // start receiving image data
                    BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                }
                // send ending buffer
                // acquire new buffer
                Buffer bufData = BufferPool.getInstance().queueDecoderToNetwork.take();
                bufData.size = 0;
                bufData.index = msg.getImageMsg().getStatus();
                BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                // report
                Profiler.reportOnRecvEnd();
                StringPool.addField("Image Data", String.format(Locale.TAIWAN, "[%d] (index: %d) %d bytes", msg.getImageMsg().getSerialNumber(), msg.getImageMsg().getStatus(), msg.getImageMsg().getByteSize()));
                Gdx.app.debug("Image Data", String.format(Locale.TAIWAN, "[%d] %d bytes", msg.getImageMsg().getSerialNumber(), msg.getImageMsg().getByteSize()));
                // send data to decoder

                break;
            case MsgEnding:
                Gdx.app.log("Dispatch", "Ending message received");
                requireStop();
                StringPool.removeField("Image Data");
                break;
            default:
                Gdx.app.error("Dispatch", "Unknown message!");
                break;
        }
    }

    private void sendEndingMessage() {
        Gdx.app.log("App", "Sending Ending message...");
        // crafting packet
        Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                .setType(Message.MessageType.MsgEnding)
                .build();
        try {
            Gdx.app.log("App", "Wait for Ending message to be sent...");
            network.blockedSendMessage(msg);
            Gdx.app.log("App", "Ending message sent");
        } catch (InterruptedException e) {
            Gdx.app.error("App", "Interrupted when wait for sending ending message");
            e.printStackTrace();
        }
    }

    public void recenter() {
        recenterDisplay();
        cardBoardApp.getCardboardView().resetHeadTracker();
    }

    public void recenterDisplay() {
        display.recenterCamera();
    }

    public void setSaveFrameRequested(boolean saveFrameRequested) {
        this.saveFrameRequested = saveFrameRequested;
    }

    public void onStopCalled() {
        StringPool.addField("App", "Shutting Down...");
        sendEndingMessage();
        setState(ShuttingDown);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                StringPool.addField("App", "Shutting Down!!");
                requireStop();
            }
        }).start();
    }

    public void onStartCalled() {
        StringPool.addField("App", "Starting");
        requireStart();
    }

    private void updateEditingModeText() {
        StringPool.addField("Editing", ConfigManager.isEditingModeEnabled() ? "Enabled" : "Disabled");
    }

    private void updateEditingModeText(float imageX, float imageY) {
        StringPool.addField("Editing", (ConfigManager.isEditingModeEnabled() ? "Enabled" : "Disabled") + ", ImageX:" + imageX + ", ImageY:" + imageY);
    }

    public void startEditingMode() {
        display.editingScreenPosition.set(0, 0);
        updateEditingModeText();
        sendEditingModeMessage(Message.EditOperation.START, 0, 0);
    }

    public void finishEditingMode() {
        updateEditingModeText();
        sendEditingModeMessage(Message.EditOperation.FINISH, 0, 0);
        display.editingScreenPosition.set(-1, -1);
    }

    private boolean editingTouchDragged(int screenX, int screenY, int pointer) {
        display.updateEditingScreenPosition(screenX, screenY);
        if (this.editingReportTime >= ConfigManager.getEditingReportInterval()) {
            this.editingReportTime = 0f;
            updateEditingModeText(display.editingImagePosition.x, display.editingImagePosition.y);
            sendEditingModeMessage(Message.EditOperation.UPDATE, display.editingImagePosition.x, display.editingImagePosition.y);
        }
        return true;
    }

    private void updateEditing() {
        if (ConfigManager.isEditingModeEnabled()) {
            this.editingReportTime += Gdx.graphics.getDeltaTime();
        }
    }


    private void sendEditingModeMessage(Message.EditOperation op, float imageX, float imageY) {
        Message.Control.Builder controlBuilder = Message.Control.newBuilder();
        controlBuilder.setEditingMsg(Message.Editing.newBuilder()
                .setOp(op)
                .setScreenX(imageX)
                .setScreenY(imageY)

        );
        // create message
        Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                .setType(Message.MessageType.MsgControl)
                .setControlMsg(controlBuilder.build()
                ).build();
        // send!
        network.sendMessageProtobufAsync(msg);
    }

    public void manuallyMoveCamera(PositionController.Direction direction){
        display.manuallyMoveCamera(direction);
    }
}
