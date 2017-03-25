package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Locale;

import StreamingFormat.Message;

import static com.badlogic.gdx.Gdx.app;
import static com.covart.streaming_prototype.StreamingPrototype.State.Running;
import static com.covart.streaming_prototype.StreamingPrototype.State.ShuttingDown;
import static com.covart.streaming_prototype.StreamingPrototype.State.Stopped;

public class StreamingPrototype extends ApplicationAdapter
        implements MasterComponentAdapter, SensorDataListener {


    enum State {
        Stopped, Running, ShuttingDown
    }

    private State state = Stopped;
    private volatile boolean startRequired = false;
    private volatile boolean stopRequired = false;

    // major component
    private DisplayBase display;
    private Network network;
    private ImageDecoderBase decoder;
    private Sensor sensor;

    StreamingPrototype(ImageDecoderBase platform_decoder){
        if(platform_decoder != null){
            decoder = platform_decoder;
        }
    }

	@Override
	public void create () {
        StringPool.addField("App", "Initializing");
        network = new Network(this);
        display = new DisplayLightField();
        sensor  = new Sensor();
        sensor.addListener(this);
        sensor.addListener(display);

        if(decoder == null){
            Gdx.app.error("App", "No platform decoder specified! Use simple decoder instead!");
            decoder = new ImageDecoderSimple();
        }

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown (int x, int y, int pointer, int button) {
                Gdx.app.log("Touch point:", "X:" + x + " , Y:" + y);
                if(x <= 135 && y <= 135) {
                    if (StreamingPrototype.this.state == Stopped) {
                        StringPool.addField("App", "Starting");
                        requireStart();
                    } else if (StreamingPrototype.this.state == Running) {
                        StringPool.addField("App", "Shutting Down...");
                        sendEndingMessage();
                        state = ShuttingDown;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                requireStop();
                            }
                        }).start();
                    }
                    return true; // return true to indicate the event was handled
                }
                else{
                    return false;
                }
            }
            @Override
            public boolean touchDragged (int screenX, int screenY, int pointer) {
                return display.touchDragged(screenX, screenY, pointer);
            }

        });
        StringPool.addField("App", "Ready for start");
    }

    @Override
    public void start() {
        stopRequired = false;
        startRequired = false;
        app.log("App","starting");
        StringPool.addField("App", "Component started");
        this.state = Running;
        BufferPool.getInstance().reset();
        Profiler.reset();
        display.disposeExistingTexture();
        network.start();
        decoder.start();
        sensor.start();
        StringPool.addField("App", "Running. Touch the screen to stop");
    }

    @Override
    public void stop() {
        stopRequired = false;
        startRequired = false;
        app.log("App","stopping");
        this.state = Stopped;
        sensor.stop();
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
	public void render () {
        if(startRequired){
            start();
        }
        if(stopRequired){
            stop();
        }
        display.updateStart();
        Profiler.generateProfilingStrings();
        display.updateEnd();
	}


    @Override
	public void dispose () {
        decoder.dispose();
        display.dispose();
        network.dispose();
	}


    private Message.StreamingMessage makeSensorPacket(Vector3 direction, Quaternion rotation) {

        Vector3 initDirection = sensor.getInitDirection();
        // crafting packet
        Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                .setType(Message.MessageType.MsgCameraInfo)
                .setCameraMsg(
                        Message.Camera.newBuilder()
                                .setDeltaVx(direction.x - initDirection.x)
                                .setDeltaVy(direction.y - initDirection.y)
                                .setDeltaVz(direction.y - initDirection.z)
                                .setSerialNumber(sensor.getSerialNumber())
                                .build()

                ).build();
        return msg;
    }

    @Override
    public void onSensorDataReady(Vector3 direction, Quaternion rotation) {
        Message.StreamingMessage msg = makeSensorPacket(direction, rotation);
        if(msg != null){
            network.sendMessageProtobufAsync(msg);
        }
    }

    @Override
    public void dispatchMessage(Message.StreamingMessage msg) throws InterruptedException {
        switch(msg.getType()){
            case MsgDefaultPos:
                Gdx.app.log("Dispatch", "DefaultPos set");
                Message.DefaultPos posMsg = msg.getDefaultPosMsg();
                sensor.setInitialDirection(posMsg.getVx(), posMsg.getVy(), posMsg.getVz());
                break;
            case MsgImage:
                // acquire new buffer
                Buffer bufData = BufferPool.getInstance().queueDecoderToNetwork.take();
                // start receiving image data
                Profiler.reportOnRecvStart();
                network.getConnection().readn(bufData.data, msg.getImageMsg().getByteSize());
                bufData.size = msg.getImageMsg().getByteSize();
                Profiler.reportOnRecvEnd();
                StringPool.addField("Image Data", String.format(Locale.TAIWAN, "[%d] %d bytes", msg.getImageMsg().getSerialNumber() ,msg.getImageMsg().getByteSize()));
                Gdx.app.debug("Image Data", String.format(Locale.TAIWAN, "[%d] %d bytes", msg.getImageMsg().getSerialNumber(), msg.getImageMsg().getByteSize()));
                // send data to decoder
                BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                break;
            case MsgEnding:
                Gdx.app.log("Dispatch","Ending message received");
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
}
