package com.covart.streaming_prototype.AutoAction;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public abstract class OnetimeAction extends Action {
    @Override
    public void start() {
        // Do nothing
    }

    @Override
    public boolean update(float deltaTime) {
        act(deltaTime);
        return false;
    }

    protected abstract void act(float deltaTime);
}
