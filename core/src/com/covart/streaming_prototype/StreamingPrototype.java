package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import static com.covart.streaming_prototype.StreamingPrototype.State.Running;
import static com.covart.streaming_prototype.StreamingPrototype.State.Stopped;

public class StreamingPrototype extends ApplicationAdapter
        implements MasterComponentAdapter {

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
	
	@Override
	public void create () {
        network = new Network(this);
        display = new Display();
        decoder = new ImageDecoderSimple();
    }

    @Override
    public void start() {
        stopRequired = false;
        startRequired = false;
        Gdx.app.log("App","starting");
        StringPool.addField("App", "Component started");
        this.state = Running;
        Profiler.reset();
        network.start();
        decoder.start();
    }

    @Override
    public void stop() {
        stopRequired = false;
        startRequired = false;
        Gdx.app.log("App","stopping");
        this.state = Stopped;
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
}
