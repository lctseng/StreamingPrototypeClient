package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.AutoAction.ApertureAction;
import com.covart.streaming_prototype.AutoAction.Executor;
import com.covart.streaming_prototype.AutoAction.ExecutorEventListener;
import com.covart.streaming_prototype.AutoAction.FocusAction;
import com.covart.streaming_prototype.AutoAction.RecenterAction;
import com.covart.streaming_prototype.AutoAction.RotationAction;
import com.covart.streaming_prototype.AutoAction.TranslationAction;
import com.covart.streaming_prototype.Image.Display;
import com.covart.streaming_prototype.Image.ImageDecoderBase;
import com.covart.streaming_prototype.Image.ImageDecoderStaticFiles;
import com.covart.streaming_prototype.Net.Network;
import com.covart.streaming_prototype.UI.EditingPanel;
import com.covart.streaming_prototype.UI.MainMenu;
import com.covart.streaming_prototype.UI.PositionController;
import com.covart.streaming_prototype.UI.UIManager;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;
import com.covart.streaming_prototype.Utils.Easing.EasingQuadInOut;
import com.covart.streaming_prototype.Utils.Profiler;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.covart.streaming_prototype.UI.PositionController.Direction;

import java.util.Locale;

import StreamingFormat.Message;

import static com.badlogic.gdx.Gdx.app;
import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.covart.streaming_prototype.StreamingPrototype.State.Running;
import static com.covart.streaming_prototype.StreamingPrototype.State.ShuttingDown;
import static com.covart.streaming_prototype.StreamingPrototype.State.Stopped;

public class StreamingPrototype extends ApplicationAdapter
        implements MasterComponentAdapter, CardBoardApplicationListener, ExecutorEventListener {


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
    public EditingPanel editingPanel;
    public MainMenu mainMenu;


    // Auto action
    public Executor autoActionExecutor;

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
        autoActionExecutor.update();
        editingPanel.checkRefreshList();
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
    public void onExecutorStart() {
        // lazy load actions
        autoActionExecutor.clearActions();
        autoActionExecutor.addAction(new RecenterAction());
        autoActionExecutor.addWait(0.5f);


        if(!autoActionExecutor.loadActionFromURL("http://xenial.csie.org:3000/" + ConfigManager.getSelectedIP().replace(":", "_") + "_action.txt")){
            // local actions

            // TODO: following are for debug action

            autoActionExecutor.addAction(new TranslationAction(Direction.RIGHT, 1, 1, new EasingLinear()));
            autoActionExecutor.addAction(new TranslationAction(Direction.LEFT, 1, 1, new EasingQuadInOut()));
            autoActionExecutor.addAction(new TranslationAction(Direction.UP, 1, 1));
            autoActionExecutor.addAction(new TranslationAction(Direction.DOWN, 1, 1));
            autoActionExecutor.addAction(new TranslationAction(Direction.FORWARD, 1, 1, new EasingQuadInOut()));
            autoActionExecutor.addAction(new TranslationAction(Direction.BACKWARD, 1, 1, new EasingLinear()), false);

            /*
            String actionText = "Action,Aperture,0\n" +
                    "Action,Aperture,0.1,3\n" +
                    "\n" +
                    "Action,Focus,1.0\n" +
                    "Action,Focus,4.5,3\n" +
                    "\n" +
                    "Action,Rotation,YAW,10,1@nowait\n" +
                    "Action,Rotation,PITCH,10,1@nowait\n" +
                    "Wait,1\n" +
                    "Action,Rotation,YAW,-20,2@nowait\n" +
                    "Action,Rotation,PITCH,-20,2@nowait\n" +
                    "Wait,2\n" +
                    "Action,Translation,RIGHT,1,1,Linear\n" +
                    "Action,Translation,LEFT,1,1,QuadInOut\n" +
                    "Action,Translation,UP,1,1\n" +
                    "Action,Translation,DOWN,1,1\n" +
                    "Action,Translation,FORWARD,1,1,QuadInOut\n" +
                    "Action,Translation,BACKWARD,1,1,Linear@nowait\n" +
                    "\n" +
                    "Action,Rotation,YAW,10,1@nowait\n" +
                    "Action,Rotation,PITCH,10,1@nowait\n" +
                    "\n" +
                    "Wait,2\n" +
                    "\n";


            autoActionExecutor.loadActionText(actionText);
            */


            /*
            autoActionExecutor.addAction(new ApertureAction(0));
            autoActionExecutor.addAction(new ApertureAction(0.1f, 3));

            autoActionExecutor.addAction(new FocusAction(1.0f));
            autoActionExecutor.addAction(new FocusAction(4.5f,3));

            autoActionExecutor.addAction(new RotationAction(RotationAction.Type.YAW, 10, 1), false);
            autoActionExecutor.addAction(new RotationAction(RotationAction.Type.PITCH, 10, 1), false);
            autoActionExecutor.addWait(1);
            autoActionExecutor.addAction(new RotationAction(RotationAction.Type.YAW, -20, 2), false);
            autoActionExecutor.addAction(new RotationAction(RotationAction.Type.PITCH, -20, 2), false);

            autoActionExecutor.addWait(2);

            autoActionExecutor.addAction(new TranslationAction(Direction.RIGHT, 1, 1, new EasingLinear()));
            autoActionExecutor.addAction(new TranslationAction(Direction.LEFT, 1, 1, new EasingQuadInOut()));
            autoActionExecutor.addAction(new TranslationAction(Direction.UP, 1, 1));
            autoActionExecutor.addAction(new TranslationAction(Direction.DOWN, 1, 1));
            autoActionExecutor.addAction(new TranslationAction(Direction.FORWARD, 1, 1, new EasingQuadInOut()));
            autoActionExecutor.addAction(new TranslationAction(Direction.BACKWARD, 1, 1, new EasingLinear()), false);

            autoActionExecutor.addAction(new RotationAction(RotationAction.Type.YAW, 10, 1), false);
            autoActionExecutor.addAction(new RotationAction(RotationAction.Type.PITCH, 10, 1), false);
            autoActionExecutor.addWait(2);
            */
        }
        // misc
        mainMenu.updateActionExecutorText();
        ConfigManager.getAutoActionState().rotationLocked = true;
    }

    @Override
    public void onExecutorStop() {
        mainMenu.updateActionExecutorText();
        ConfigManager.getAutoActionState().rotationLocked = false;
    }

    @Override
    public void onExecutorUpdateEnded() {

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
        UIManager.getInstance().registerUI(mainMenu = new MainMenu());
        UIManager.getInstance().registerUI(positionController = new PositionController());
        UIManager.getInstance().registerUI(editingPanel = new EditingPanel());

        StringPool.addField("App", "Ready for start");
        updateEditingModeText();

        // setup executor
        autoActionExecutor = new Executor(this);
        autoActionExecutor.setWaitByDefault(true);
        autoActionExecutor.setTimeFactor(1f);


    }

    private void initializeInput() {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(UIManager.getInstance().getInputProcessor());

        InputAdapter localInput = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (ConfigManager.isEditingModeEnabled()) {
                    return editingTouchDown(screenX, screenY);
                } else {
                    return false;
                }
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (ConfigManager.isEditingModeEnabled()) {
                    return editingTouchUp(screenX, screenY);
                } else {
                    return false;
                }
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (ConfigManager.isEditingModeEnabled()) {
                    return editingTouchDragged(screenX, screenY);
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
                .setDeltaX(clamp(position.x / 2f, -1.0f, 1.0f))
                .setDeltaY(clamp(position.y / 2f, -1.0f, 1.0f))
                .setDeltaZ(position.z)
                .setDeltaVx(direction.x)
                .setDeltaVy(direction.y)
                .setDeltaVz(direction.z)
                .setSerialNumber(0)
                .setImageQuality(ConfigManager.isHighQualityImagesNeeded() ? Message.ImageQuality.HIGH : Message.ImageQuality.LOW);

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
        Message.StreamingMessage msg = makeSensorPacket(display.getLastEyePosition(), display.getMainCamera().direction);
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
                    bufData.imageTypeValue = msg.getImageMsg().getImageTypeValue();

                    // start receiving image data
                    BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                }
                // send ending buffer
                // acquire new buffer
                Buffer bufData = BufferPool.getInstance().queueDecoderToNetwork.take();
                bufData.size = 0;
                bufData.index = msg.getImageMsg().getStatus();
                bufData.imageTypeValue = msg.getImageMsg().getImageTypeValue();
                BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                // report
                Profiler.reportOnRecvEnd();
                StringPool.addField("Image Data", String.format(Locale.TAIWAN, "[%d] (index: %d) %d bytes", msg.getImageMsg().getSerialNumber(), msg.getImageMsg().getStatus(), msg.getImageMsg().getByteSize()));
                Gdx.app.debug("Image Data", String.format(Locale.TAIWAN, "[%d] %d bytes", msg.getImageMsg().getSerialNumber(), msg.getImageMsg().getByteSize()));
                // send data to decoder

                break;
            case MsgControl:
                Message.Editing editMsg = msg.getControlMsg().getEditingMsg();
                if(editMsg != null){
                    if(editMsg.getOp() == Message.EditOperation.MODEL_LIST){
                        ConfigManager.setEditingModelIdList(editMsg.getModelIdsList());
                        editingPanel.setNeedRefreshList(true);
                        display.prepareForEditingMode();
                    }
                }
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
        float imageX = ConfigManager.getImageWidth() -  display.editingImagePosition.x;
        float imageY = display.editingImagePosition.y;
        StringPool.addField("Editing", (ConfigManager.isEditingModeEnabled() ? "Enabled" : "Disabled") + ", ImageX:" + imageX + ", ImageY:" + imageY);
    }

    public void startEditingMode() {
        display.editingScreenPosition.set(-1, -1);
        updateEditingModeText();
        sendEditingOpMessage(Message.EditOperation.START);
        editingPanel.show();
    }

    public void finishEditingMode() {
        updateEditingModeText();
        sendEditingOpMessage(Message.EditOperation.FINISH);
        display.editingScreenPosition.set(-1, -1);
        editingPanel.hide();
        ConfigManager.setEditingModelIdList(null);
        editingPanel.setNeedRefreshList(true);
    }

    private boolean editingTouchDragged(int screenX, int screenY) {
        display.updateEditingScreenPosition(screenX, screenY);
        if (this.editingReportTime >= ConfigManager.getEditingReportInterval()) {
            this.editingReportTime = 0f;
            updateEditingModeText();
            sendEditingUpdateMessage(display.editingImagePosition.x, display.editingImagePosition.y);
        }
        return true;
    }

    private boolean editingTouchDown(int screenX, int screenY){
        editingTouchDragged(screenX, screenY);
        if(ConfigManager.getEditingCurrentModelIndex() >= 0){
            display.setEditingPositionFollowCursor(true);
        }
        return true;
    }

    private boolean editingTouchUp(int screenX, int screenY){
        display.setEditingPositionFollowCursor(false);
        return true;
    }


    private void updateEditing() {
        if (ConfigManager.isEditingModeEnabled()) {
            this.editingReportTime += Gdx.graphics.getDeltaTime();
        }
    }

    private void sendEditingOpMessage(Message.EditOperation op){
        Message.Editing.Builder builder = Message.Editing.newBuilder()
                .setOp(op);
        sendEditingModeMessage(builder);
    }

    private void sendEditingUpdateMessage(float imageX, float imageY){
        Message.Editing.Builder builder = Message.Editing.newBuilder()
                .setOp(Message.EditOperation.UPDATE)
                .setScreenX(ConfigManager.getImageWidth() - imageX) // TODO: find out why we need to reverse the X, may be due to reversed up vector
                .setScreenY(imageY);
        sendEditingModeMessage(builder);
    }

    public  void onEditingModelChanged(int lastIndex){
        sendEditingSetModelIdMessage(ConfigManager.getEditingCurrentModelId());
        StringPool.addField("Model ID", "" + ConfigManager.getEditingCurrentModelId());
        if(ConfigManager.getEditingCurrentModelId() >= 0){
            if(lastIndex >= 0) {
                // this happens when directly change model
                display.finishEditingModel(lastIndex);
            }
            display.startEditingModel();
        }
        else{
            display.finishEditingModel(lastIndex);
        }
    }

    private void sendEditingSetModelIdMessage(int modelId){
        Message.Editing.Builder builder = Message.Editing.newBuilder()
                .setOp(Message.EditOperation.SET_MODEL_ID)
                .setModelId(modelId);
        sendEditingModeMessage(builder);
    }


    private void sendEditingModeMessage(Message.Editing.Builder builder) {
        Message.Control.Builder controlBuilder = Message.Control.newBuilder();
        controlBuilder.setEditingMsg(
                builder
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
