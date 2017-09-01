package com.covart.streaming_prototype.AutoAction;

import static com.badlogic.gdx.math.MathUtils.clamp;

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



    private float lastValue;
    private boolean refreshValuesOnStart = false;

    protected ContinuousAction(float startValue, float endValue, float duration){
        this.startValue = startValue;
        this.endValue = endValue;
        this.diffValue = endValue - startValue;
        this.duration = duration;
    }

    // start/end value is not provided by constructor
    // must override prepareStartEndValue in descendant
    protected ContinuousAction(float duration){
        refreshValuesOnStart = true;
        this.duration = duration;
    }

    protected void prepareStartEndValue(){
        this.startValue = 0f;
        this.endValue = 0f;
    }


    @Override
    public float getWaitTime() {
        return duration;
    }

    @Override
    public void start() {
        if(refreshValuesOnStart) {
            prepareStartEndValue();
            this.diffValue = endValue - startValue;
        }
        currentValue = startValue;
        lastValue = startValue;
        executedTime = 0f;

    }

    @Override
    public boolean update(float deltaTime) {
        executedTime += deltaTime;
        float oldCurrentValue = currentValue;
        float ratio;
        if(duration <= 0f){
            ratio = 1f;
        }
        else{
            ratio = clamp(executedTime/duration,0f,1f);
        }
        currentValue = startValue + diffValue * ratio;
        float stepValue = currentValue - lastValue;
        lastValue = oldCurrentValue;
        act(stepValue);
        return isEnded();
    }

    protected abstract void act(float stepValue);

    private boolean isEnded(){
        if(duration <= 0f){
            // for duration is zero, must immediately finished
            return true;
        }
        else if(startValue > endValue){
            // decreasing
            return currentValue <= endValue;
        }
        else{
            // increasing
            return currentValue >= endValue;
        }
    }
}
