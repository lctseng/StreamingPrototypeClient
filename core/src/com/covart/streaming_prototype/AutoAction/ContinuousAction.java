package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.Utils.Easing.EasingBase;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;
import com.covart.streaming_prototype.Utils.Easing.EasingQuadInOut;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public abstract class ContinuousAction extends Action {
    protected float startValue;
    protected float endValue;
    protected float duration;
    protected float currentValue;
    protected float executedTime;

    private EasingBase easing;


    private float lastValue;
    private boolean refreshValuesOnStart = false;

    protected ContinuousAction(float startValue, float endValue, float duration){
        this.startValue = startValue;
        this.endValue = endValue;
        this.duration = duration;
        this.easing = getDefaultEasing();
        this.easing.setValues(startValue, endValue, duration);
    }

    protected ContinuousAction(float startValue, float endValue, float duration, EasingBase easing){
        this.startValue = startValue;
        this.endValue = endValue;
        this.duration = duration;
        if(easing != null){
            this.easing = easing;
        }
        else{
            this.easing = getDefaultEasing();
        }
        this.easing.setValues(startValue, endValue, duration);
    }

    protected EasingBase getDefaultEasing(){
        return new EasingQuadInOut();
    }

    // start/end value is not provided by constructor
    // must override prepareStartEndValue in descendant
    protected ContinuousAction(float duration){
        refreshValuesOnStart = true;
        this.duration = duration;
        this.easing = getDefaultEasing();
    }

    protected ContinuousAction(float duration, EasingBase easing){
        refreshValuesOnStart = true;
        this.duration = duration;
        if(easing != null){
            this.easing = easing;
        }
        else{
            this.easing = getDefaultEasing();
        }
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
            this.easing.setValues(startValue, endValue, duration);
        }
        currentValue = startValue;
        lastValue = startValue;
        executedTime = 0f;

    }

    @Override
    public boolean update(float deltaTime) {
        executedTime += deltaTime;
        float oldCurrentValue = currentValue;
        currentValue = easing.valueAt(executedTime);
        float stepValue = currentValue - lastValue;
        lastValue = oldCurrentValue;
        act(stepValue);
        return isEnded();
    }

    protected abstract void act(float stepValue);

    private boolean isEnded() {
        // for duration is zero, must immediately finished
        return duration <= 0f || executedTime >= duration;
    }
}
