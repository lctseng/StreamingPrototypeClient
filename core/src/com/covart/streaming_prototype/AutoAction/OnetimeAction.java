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
    protected boolean updateAction(float deltaTime) {
        act(deltaTime);
        return true;
    }

    @Override
    public float getWaitTime() {
        return 0f;
    }

    protected abstract void act(float deltaTime);
}
