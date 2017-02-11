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

    // debug
    private int count;

    // major component
    private Display display;
    private Network network;
	
	@Override
	public void create () {
        network = new Network(this);
        display = new Display();
        count = 0;

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
    }

    @Override
    public void stop() {
        stopRequired = false;
        startRequired = false;
        Gdx.app.log("App","stopping");
        this.state = Stopped;
        network.stop();

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
        display.updateEnd();
	}
	
	@Override
	public void dispose () {
        display.dispose();
        network.dispose();
	}
}
