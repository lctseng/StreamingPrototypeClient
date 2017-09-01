package com.covart.streaming_prototype.Utils.Easing;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public abstract class EasingBase {

    protected float startValue;
    protected float endValue;
    protected float diffValue;
    protected float duration;



    public EasingBase(){

    }

    public EasingBase(float startValue, float endValue, float duration){
        setValues(startValue, endValue, duration);
    }


    public void setValues(float startValue, float endValue, float duration){
        this.startValue = startValue;
        this.endValue = endValue;
        this.diffValue = endValue - startValue;
        this.duration = duration;
    }


    public float valueAt(float time){
        if(time < 0f){
            return this.startValue;
        }
        else if(duration <= 0f || time >= duration){
            return this.endValue;
        }
        else{
            return valueAtWithoutBoundaryCheck(time);
        }
    }

    public abstract float valueAtWithoutBoundaryCheck(float time);
}
