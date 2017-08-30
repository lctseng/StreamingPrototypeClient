package com.covart.streaming_prototype.AutoAction;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public abstract class ContinuousAction extends Action {
    protected float startValue;
    protected float endValue;
    protected float diffValue;
    protected float duration;
    protected float currentValue;
    protected float executedTime;

    protected ContinuousAction(float startValue, float endValue, float duration){
        this.startValue = startValue;
        this.endValue = endValue;
        this.diffValue = endValue = startValue;
        this.duration = duration;
    }

    @Override
    public void start() {
        currentValue = startValue;
        executedTime = 0f;

    }

    @Override
    public boolean update(float deltaTime) {
        executedTime += deltaTime;
        currentValue = startValue + diffValue * (executedTime/duration);
        act(currentValue);
        if(isEnded()){
            return true;
        }
        else{
            return false;
        }
    }

    protected abstract void act(float currentValue);

    private boolean isEnded(){
        if(startValue > endValue){
            // decreasing
            return currentValue <= endValue;
        }
        else{
            // increasing
            return currentValue >= endValue;
        }
    }
}
