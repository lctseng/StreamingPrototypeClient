package com.covart.streaming_prototype;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import static com.covart.streaming_prototype.StreamingPrototype.State.Running;
import static com.covart.streaming_prototype.StreamingPrototype.State.Stopped;

public class StreamingPrototype extends ApplicationAdapter
        implements Component {

    enum State {
        Stopped, Running
    }

    private State state = Stopped;

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
        Gdx.app.log("App","starting");
        StringPool.addField("App", "Component started");
        this.state = Running;
        Profiler.reset();
        network.start();
    }

    @Override
    public void stop() {
        Gdx.app.log("App","stopping");
        this.state = Stopped;
        network.stop();

    }

	@Override
	public void render () {
        display.updateStart();
        if(state == Stopped){
            // show touch bar
            StringPool.addField("App", "Stopped. Touch the screen to start the components");
            if(Gdx.input.isTouched()){
                start();
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
