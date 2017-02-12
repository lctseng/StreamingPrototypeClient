package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import java.util.Locale;

import StreamingFormat.Message;

import static com.badlogic.gdx.Gdx.app;
import static com.covart.streaming_prototype.StreamingPrototype.State.Running;
import static com.covart.streaming_prototype.StreamingPrototype.State.Stopped;

public class StreamingPrototype extends ApplicationAdapter
        implements MasterComponentAdapter, SensorDataListener {


    enum State {
        Stopped, Running
    }

    private State state = Stopped;
    private volatile boolean startRequired = false;
    private volatile boolean stopRequired = false;

    // major component
    private Display display;
    private Network network;
    private ImageDecoderBase decoder;
    private Sensor sensor;
	
	@Override
	public void create () {
        network = new Network(this);
        display = new Display();
        decoder = new ImageDecoderLZ4();
        sensor  = new Sensor(this);
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
        Profiler.reset();
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
        if(state == Stopped){
            // show touch bar
            StringPool.addField("App", "Stopped. Touch the screen to start the components");
            if(Gdx.input.isTouched()){
                requireStart();
            }
        }
        Profiler.generateProfilingStrings();
        display.updateEnd();
	}
	
	@Override
	public void dispose () {
        decoder.dispose();
        display.dispose();
        network.dispose();
	}

    @Override
    public void onSensorMessageReady(Message.StreamingMessage msg) {
        network.sendMessageProtobufAsync(msg);
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
                byte[] bufData = BufferPool.getInstance().queueDecoderToNetwork.take();
                // start receiving image data
                Profiler.reportOnRecvStart();
                network.getConnection().readn(bufData, msg.getImageMsg().getByteSize());
                Profiler.reportOnRecvEnd();
                StringPool.addField("Image Data", String.format(Locale.TAIWAN, " %d bytes", msg.getImageMsg().getByteSize()));
                // send data to decoder
                BufferPool.getInstance().queueNetworkToDecoder.put(bufData);
                break;
            case MsgEnding:
                requireStop();
                StringPool.removeField("Image Data");
                break;
            default:
                Gdx.app.error("Dispatch", "Unknown message!");
                break;
        }
    }
}
