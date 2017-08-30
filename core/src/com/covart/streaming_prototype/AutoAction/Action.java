package com.covart.streaming_prototype.AutoAction;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public abstract class Action {
    public float startTime = 0f;


    public abstract void start();

    public abstract boolean update(float deltaTime);
}
