package com.covart.streaming_prototype.AutoAction;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public abstract class Action {
    public float startTime = 0f;
    public float offset = 0f;

    public abstract void start();

    public void requestStart(){
        if(offset <= 0f){
            start();
        }
    }

    public boolean update(float deltaTime){
        if(offset <= 0f){
            return updateAction(deltaTime);
        }
        else{
            offset -= deltaTime;
            if(offset <= 0f){
                start();
                if(updateOnStart()){
                    return updateAction(deltaTime);
                }
            }
            return false;
        }
    }

    protected abstract boolean updateAction(float deltaTime);

    public abstract float getWaitTime();

    public boolean updateOnStart(){
        return offset <= 0f;
    }
}
